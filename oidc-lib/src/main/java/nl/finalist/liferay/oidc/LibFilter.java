package nl.finalist.liferay.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
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
public class LibFilter  {

    public static final String REQ_PARAM_CODE = "code";
    public static final String REQ_PARAM_STATE = "state";

    /**
     * Property that is used to configure whether to enable OpenID Connect auth
     */
    public static final String PROPKEY_ENABLE_OPEN_IDCONNECT = "openidconnect.enableOpenIDConnect";
    
    public enum FilterResult {
    	CONTINUE_CHAIN, 
    	BREAK_CHAIN;
    }
    
    
    /**
     * Session attribute name containing the UserInfo
     */
    public static final String OPENID_CONNECT_SESSION_ATTR = "OpenIDConnectUserInfo";

    /**
     * Location of the authorization service (request token)
     */
    public final String AUTHORIZATION_LOCATION;
    
    /**
     * Location of the token service (exchange code for token)
     */
    public final String TOKEN_LOCATION;

    /**
     * UserInfo endpoint
     */
    public final String PROFILE_URI;
    
    /**
     * SSO logout endpoint (of offered)
     */
    public final String SSO_LOGOUT_URI;
    
    /**
     * SSO logout endpoint (of offered)
     */
    public final String SSO_LOGOUT_PARAM;
    
    /**
     * SSO logout endpoint (of offered)
     */
    public final String SSO_LOGOUT_VALUE;

    /**
     * Name of the issuer, to be confirmed with the contents of the ID token
     */
    public final String ISSUER;

    /**
     * OAuth client id.
     */
    private final String CLIENT_ID;

    /**
     * OAuth client secret.
     */
    private final String SECRET;

    /**
     * Scope of access token to request from OIDC Provider
     */
    private final String SCOPE;

    /**
     * Whether this OpenID Connect filter/autologin is enabled or not.
     */
    public final boolean USE_OPENID_CONNECT;

    private final LiferayAdapter liferay;

    public LibFilter(LiferayAdapter liferay) {
        this.liferay = liferay;


        AUTHORIZATION_LOCATION = liferay.getPortalProperty("openidconnect.authorization-location");
        TOKEN_LOCATION = liferay.getPortalProperty("openidconnect.token-location");
        SCOPE = liferay.getPortalProperty("openidconnect.scope", "openid profile email");
        PROFILE_URI = liferay.getPortalProperty("openidconnect.profile-uri");
        SSO_LOGOUT_URI = liferay.getPortalProperty("openidconnect.sso-logout-uri", ""); // Important: Do not use NULL as default value since this would cause a NPE deep down!
        SSO_LOGOUT_PARAM = liferay.getPortalProperty("openidconnect.sso-logout-param", ""); // Important: Do not use NULL as default value since this would cause a NPE deep down!
        SSO_LOGOUT_VALUE = liferay.getPortalProperty("openidconnect.sso-logout-value", ""); // Important: Do not use NULL as default value since this would cause a NPE deep down!
        USE_OPENID_CONNECT = liferay.getPortalProperty(PROPKEY_ENABLE_OPEN_IDCONNECT, false);
        ISSUER = liferay.getPortalProperty("openidconnect.issuer");
        CLIENT_ID = liferay.getPortalProperty("openidconnect.client-id");
        SECRET = liferay.getPortalProperty("openidconnect.secret");
    }


    /**
     * Filter the request. 
     * <br><br>LOGIN:<br> 
     * The first time this filter gets hit, it will redirect to the OP.
     * Second time it will expect a code and state param to be set, and will exchange the code for an access token.
     * Then it will request the UserInfo given the access token.
     * <br>--
     * Result: the OpenID Connect 1.0 flow.
     * <br><br>LOGOUT:<br>
     * When the filter is hit and according values for SSO logout are set, it will redirect to the OP logout resource.
     * From there the request should be redirected "back" to a public portal page or the public portal home page. 
     *
     * @param request the http request
     * @param response the http response
     * @param filterChain the filterchain
     * @throws Exception according to interface.
     * @return FilterResult, to be able to distinct between continuing the chain or breaking it.
     */
    protected FilterResult processFilter(HttpServletRequest request, HttpServletResponse response, FilterChain 
            filterChain) throws Exception {
        // If the plugin is not enabled, short circuit immediately
        if (!USE_OPENID_CONNECT) {
            liferay.trace("OpenIDConnectFilter deployed, altough not activated. Will skip it.");
            return FilterResult.CONTINUE_CHAIN;
        }

        liferay.trace("In processFilter()...");

		String pathInfo = request.getPathInfo();

		if (null != pathInfo) {
			if (pathInfo.contains("/portal/login")) {
		        if (!StringUtils.isBlank(request.getParameter(REQ_PARAM_CODE))
		                && !StringUtils.isBlank(request.getParameter(REQ_PARAM_STATE))) {

		            if (!isUserLoggedIn(request)) {
		                // LOGIN: Second time it will expect a code and state param to be set, and will exchange the code for an access token.
		                liferay.trace("About to exchange code for access token");
		                exchangeCodeForAccessToken(request);
		            } else {
		                liferay.trace("subsequent run into filter during openid conversation, but already logged in." +
		                        "Will not exchange code for token twice.");
		            }
		        } else {
		        	// LOGIN: The first time this filter gets hit, it will redirect to the OP.
		            liferay.trace("About to redirect to OpenID Provider");
		            redirectToLogin(request, response, CLIENT_ID);
		            // no continuation of the filter chain; we expect the redirect to commence.
		            return FilterResult.BREAK_CHAIN;
		        }
			} 
			else
			if (pathInfo.contains("/portal/logout")) {
				if (null != SSO_LOGOUT_URI && SSO_LOGOUT_URI.length() > 0 && isUserLoggedIn(request)) {
					
					liferay.trace("About to logout from SSO by redirect to " + SSO_LOGOUT_URI);
			        // LOGOUT: If Portal Logout URL is requested, redirect to OIDC Logout resource afterwards to globally logout.
			        // From there, the request should be redirected back to the Liferay portal home page.
					request.getSession().invalidate();
					redirectToLogout(request, response, SSO_LOGOUT_URI, SSO_LOGOUT_PARAM, SSO_LOGOUT_VALUE);
		            // no continuation of the filter chain; we expect the redirect to commence.
		            return FilterResult.BREAK_CHAIN;
				}
			}
		}
        // continue chain
		return FilterResult.CONTINUE_CHAIN;

    }

    protected void exchangeCodeForAccessToken(HttpServletRequest request) throws IOException {
        try {
            String codeParam = request.getParameter(REQ_PARAM_CODE);
            String stateParam = request.getParameter(REQ_PARAM_STATE);

            String expectedState = generateStateParam(request);
            if (!expectedState.equals(stateParam)) {
                liferay.info("Provided state parameter '" + stateParam + "' does not equal expected '"
                        + expectedState + "', cannot continue.");
                throw new IOException("Invalid state parameter");
            }

            OAuthClientRequest tokenRequest = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(CLIENT_ID)
                    .setClientSecret(SECRET)
                    .setCode(codeParam)
                    .setRedirectURI(getRedirectUri(request))
                    .buildBodyMessage();
            liferay.debug("Token request to uri: " + tokenRequest.getLocationUri());

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OpenIdConnectResponse oAuthResponse = oAuthClient.accessToken(tokenRequest, OpenIdConnectResponse.class);

            String accessToken = oAuthResponse.getAccessToken();

            if (!oAuthResponse.checkId(ISSUER, CLIENT_ID)) {
                liferay.warn("The token was not valid: " + oAuthResponse.toString());
                return;
            }

            // The only API to be enabled (in case of Google) is Google+.
            OAuthClientRequest userInfoRequest = new OAuthBearerClientRequest(PROFILE_URI)
                    .setAccessToken(accessToken).buildHeaderMessage();
            liferay.trace("UserInfo request to uri: " + userInfoRequest.getLocationUri());
            OAuthResourceResponse userInfoResponse =
                    oAuthClient.resource(userInfoRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

            liferay.debug("Response from UserInfo request: " + userInfoResponse.getBody());
            Map openIDUserInfo = new ObjectMapper().readValue(userInfoResponse.getBody(), HashMap.class);

            liferay.debug("Setting OpenIDUserInfo object in session: " + openIDUserInfo);
            request.getSession().setAttribute(OPENID_CONNECT_SESSION_ATTR, openIDUserInfo);

        } catch (OAuthSystemException | OAuthProblemException e) {
            throw new IOException("While exchanging code for access token and retrieving user info", e);
        }
    }

    protected void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String clientId) throws
            IOException {
        try {
        	String ui_locales = null;
        	
    		Cookie[] cookies = request.getCookies(); // look for GUEST_LANGUAGE_ID
    		for (Cookie cookie : cookies) {
    			liferay.trace("redirectToLogin: cookie: " + cookie.getName() + " = " + cookie.getValue());
    			if ("GUEST_LANGUAGE_ID".equals(cookie.getName())) {
    				String guestLanguageId = cookie.getValue();
    				String[] guestLocale = guestLanguageId.split("_");
    				ui_locales = guestLanguageId; // full locale, just as-is: 3-zone OR 2-zone OR 1-zone locale
    				if (guestLocale.length > 2) { // we got 3-zone locale: language_COUNTRY_REGION: Add "langauge_COUNTRY"
    					ui_locales += " " + guestLocale[0] + "_" + guestLocale[1];
    				}
    				if (guestLocale.length > 1) { // we got (3- or) 2-zone locale: language_COUNTRY: Add "language"
    					ui_locales += " " + guestLocale[0];
    				}
        			liferay.trace("redirectToLogin: use for ui_locales: " + ui_locales);
    			}
    		}
    		
    		if (null == ui_locales) { // no GUEST_LANGUAGE_ID cookie available:
        		ui_locales = request.getServletPath().substring(1); // may be /c (default locale, useless) or /en (requested locale, useful) or /xy (useful) ...
    		}
    		
    		if (null == ui_locales || ui_locales.length() < 2) { // skip values being too short to meet https://tools.ietf.org/html/rfc5646
    			// TODO: Improve locale recognition according to syntax given in RFC-5646
    			ui_locales = request.getLocale().getLanguage();
    		}
    		liferay.trace("redirectToLogin: ui_locales: " + ui_locales);
    		
            OAuthClientRequest oAuthRequest = OAuthClientRequest
                    .authorizationLocation(AUTHORIZATION_LOCATION)
                    .setClientId(clientId)
                    .setRedirectURI(getRedirectUri(request))
                    .setResponseType("code")
                    .setScope(SCOPE)
                    .setState(generateStateParam(request))
                    .setParameter("ui_locales", ui_locales) // see http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
                    .buildQueryMessage();
            liferay.debug("Redirecting to URL: " + oAuthRequest.getLocationUri());
            response.sendRedirect(oAuthRequest.getLocationUri());
        } catch (OAuthSystemException e) {
            throw new IOException("While redirecting to OP for SSO login", e);
        }
    }
    
    protected void redirectToLogout(HttpServletRequest request, HttpServletResponse response, 
    		String logoutUrl, String logoutUrlParamName, String logoutUrlParamValue) throws
    		IOException {
			// build logout URL and append params if present
			if (StringUtils.isNotEmpty(logoutUrlParamName) && StringUtils.isNotEmpty(logoutUrlParamValue)) {
				logoutUrl = addParameter(logoutUrl, logoutUrlParamName, logoutUrlParamValue);
			}
			liferay.debug("On " + request.getRequestURL() + " redirect to OP for SSO logout: " + logoutUrl);
			response.sendRedirect(logoutUrl);
    }

    protected String getRedirectUri(HttpServletRequest request) {
        String completeURL = liferay.getCurrentCompleteURL(request);
        // remove parameters
        return completeURL.replaceAll("\\?.*", "");
    }

    protected String generateStateParam(HttpServletRequest request) {
        return DigestUtils.md5Hex(request.getSession().getId());
    }
    
    protected boolean isUserLoggedIn(HttpServletRequest request) {
        return liferay.isUserLoggedIn(request);
    }

    protected String addParameter(String url, String param, String value) {
    	String anchor = "";
    	int posOfAnchor = url.indexOf('#');
    	if (posOfAnchor > -1) {
    		anchor = url.substring(posOfAnchor);
    		url = url.substring(0, posOfAnchor);
    	}
    	
		StringBuffer sb = new StringBuffer();
		sb.append(url);
		if (url.indexOf('?') < 0) {
			sb.append('?');
		} else 
		if (!url.endsWith("?") && !url.endsWith("&")) {
			sb.append('&');
		}
		sb.append(param);
		sb.append('=');
		try {
			sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
		} catch (UnsupportedEncodingException e) {
			sb.append(value);
		}
		sb.append(anchor);

    	return sb.toString() + anchor;
    }

}
