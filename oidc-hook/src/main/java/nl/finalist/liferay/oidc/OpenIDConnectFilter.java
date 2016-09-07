package nl.finalist.liferay.oidc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

/**
 * Servlet filter that initiates OpenID Connect logins, and handles the resulting flow, until and including the
 * UserInfo request. It saves the UserInfo in a session attribute, to be examined by an AutoLogin.
 */
public class OpenIDConnectFilter extends BaseFilter {

    public static final String REQ_PARAM_CODE = "code";
    public static final String REQ_PARAM_STATE = "state";
    private static final Log LOG = LogFactoryUtil.getLog(OpenIDConnectFilter.class);

    /**
     * Session attribute name containing the UserInfo
     */
    public static final String OPENID_CONNECT_SESSION_ATTR = "OpenIDConnectUserInfo";

    /**
     * Location of the authorization service (request token)
     */
    public static final String AUTHORIZATION_LOCATION = PropsUtil.get("fnv.sso.authorization-location");

    /**
     * Location of the token service (exchange code for token)
     */
    public static final String TOKEN_LOCATION = PropsUtil.get("fnv.sso.token-location");

    /**
     * UserInfo endpoint
     */
    public static final String PROFILE_URI = PropsUtil.get("fnv.sso.profile-uri");

    /**
     * Name of the issuer, to be confirmed with the contents of the ID token
     */
    public static final String ISSUER = PropsUtil.get("fnv.sso.issuer");

    /**
     * OAuth client id.
     */
    private static final String CLIENT_ID = PropsUtil.get("fnv.sso.client-id");

    /**
     * OAuth client secret.
     */
    private static final String SECRET = PropsUtil.get("fnv.sso.secret");

    /**
     * Whether this OpenID Connect filter/autologin is enabled or not.
     */
    public static final boolean USE_OPENID_CONNECT = GetterUtil.getBoolean(PropsUtil.get("fnv.sso.enableOpenIDConnect"));

    @Override
    protected Log getLog() {
        return LOG;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        super.init(filterConfig);
        LOG.debug("Initializing OpenIDConnectFilter (enabled: " + USE_OPENID_CONNECT + ")");
    }

    /**
     * Filter the request. The first time this filter gets hit, it will redirect to the OP.
     * Second time it will expect a code and state param to be set, and will exchange the code for an access token.
     * Then it will request the UserInfo given the access token.
     * Result: the OpenID Connect 1.0 flow.
     *
     * @param request the http request
     * @param response the http response
     * @param filterChain the filterchain
     * @throws Exception according to interface.
     */
    protected void processFilter(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception {

        // If the plugin is not enabled, short circuit immediately
        if (!USE_OPENID_CONNECT) {
            LOG.trace("OpenIDConnectFilter deployed, altough not activated. Will skip it.");
            processFilter(OpenIDConnectFilter.class, request, response, filterChain);
            return;
        }

        LOG.trace("In processFilter()...");

        if (!Validator.isBlank(request.getParameter(REQ_PARAM_CODE))
                && !Validator.isBlank(request.getParameter(REQ_PARAM_STATE))) {

            if (!isUserLoggedIn(request)) {
                LOG.trace("About to exchange code for access token");
                exchangeCodeForAccessToken(request);
            } else {
                LOG.trace("subsequent run into filter during openid conversation, but already logged in." +
                        "Will not exchange code for token twice.");
            }
            // continue chain
            processFilter(OpenIDConnectFilter.class, request, response, filterChain);
        } else {
            LOG.trace("About to redirect to OpenID Provider");
            redirectToLogin(request, response, CLIENT_ID);
            // no continuation of the filter chain; we expect the redirect to commence.
        }
    }

    protected void exchangeCodeForAccessToken(HttpServletRequest request) throws IOException {
        try {
            String codeParam = request.getParameter(REQ_PARAM_CODE);
            String stateParam = request.getParameter(REQ_PARAM_STATE);

            String expectedState = generateStateParam(request);
            if (!expectedState.equals(stateParam)) {
                LOG.info("Provided state parameter '" + stateParam + "' does not equal expected '"
                        + expectedState + "', cannot continue.");
                throw new IOException("Invalid state parameter");
            }

            OAuthClientRequest tokenRequest = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(CLIENT_ID)
                    .setClientSecret(SECRET)
                    .setCode(codeParam)
                    .setRedirectURI(getRedirectUri(request))
                    .buildQueryMessage();
            LOG.debug("Token request to uri: " + tokenRequest.getLocationUri());

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OpenIdConnectResponse oAuthResponse = oAuthClient.accessToken(tokenRequest, OpenIdConnectResponse.class);

            String accessToken = oAuthResponse.getAccessToken();

            if (!oAuthResponse.checkId(ISSUER, CLIENT_ID)) {
                LOG.warn("The token was not valid: " + oAuthResponse.toString());
                return;
            }

            // The only API to be enabled (in case of Google) is Google+.
            OAuthClientRequest userInfoRequest = new OAuthBearerClientRequest(PROFILE_URI)
                    .setAccessToken(accessToken).buildHeaderMessage();
            LOG.trace("UserInfo request to uri: " + userInfoRequest.getLocationUri());
            OAuthResourceResponse userInfoResponse =
                    oAuthClient.resource(userInfoRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

            LOG.debug("Response from UserInfo request: " + userInfoResponse.getBody());
            Map openIDUserInfo = new ObjectMapper().readValue(userInfoResponse.getBody(), HashMap.class);

            LOG.debug("Setting OpenIDUserInfo object in session: " + openIDUserInfo);
            request.getSession().setAttribute(OPENID_CONNECT_SESSION_ATTR, openIDUserInfo);

        } catch (OAuthSystemException | OAuthProblemException e) {
            throw new IOException("While exchanging code for access token and retrieving user info", e);
        }
    }

    protected void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String clientId) throws
            IOException {
        try {
            OAuthClientRequest oAuthRequest = OAuthClientRequest
                    .authorizationLocation(AUTHORIZATION_LOCATION)
                    .setClientId(clientId)
                    .setRedirectURI(getRedirectUri(request))
                    .setResponseType("code")
                    .setScope("openid profile email FNV FNV-lidstatus") // TODO Make configurable?
                    .setState(generateStateParam(request))
                    .buildQueryMessage();
            LOG.debug("Redirecting to URL: " + oAuthRequest.getLocationUri());
            response.sendRedirect(oAuthRequest.getLocationUri());
        } catch (OAuthSystemException e) {
            throw new IOException("While redirecting to OP", e);
        }
    }

    protected String getRedirectUri(HttpServletRequest request) {
        String completeURL = PortalUtil.getCurrentCompleteURL(request);
        // remove parameters
        return completeURL.replaceAll("\\?.*", "");
    }

    protected String generateStateParam(HttpServletRequest request) {
        return DigestUtils.md5Hex(request.getSession().getId());
    }
    
    protected boolean isUserLoggedIn(HttpServletRequest request) {
        try {
            return PortalUtil.getUser(request) != null;
        } catch (PortalException | SystemException e) {
            return false;
        }
    }
}
