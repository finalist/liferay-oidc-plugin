package nl.finalist.liferay.oidc;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

/**
 * AutoLogin for OpenID Connect 1.0
 * This class should be used in tandem with the OpenIDConnectFilter. That filter will do the OAuth conversation and
 * set a session attribute containing the UserInfo object (the claims).
 * This AutoLogin will use the claims to find a corresponding Liferay user or create a new one if none found.
 */
public class LibAutoLogin {


    /**
     * The naming of the OpenID Connect UserInfo attributes
     */
    private static final String USERINFO_ATTR_FIRST_NAME = "given_name";
    private static final String USERINFO_ATTR_LAST_NAME = "family_name";
    private static final String USERINFO_ATTR_EMAIL = "email";

    private final OIDCLiferay liferay;

    public LibAutoLogin(OIDCLiferay liferay) {
        this.liferay = liferay;
        liferay.info("Initialized LibAutoLogin with Liferay API: " + liferay.getClass().getName());
    }


    public String[] doLogin(HttpServletRequest request, HttpServletResponse response) {
        if (!liferay.getPortalProperty(LibFilter.PROPKEY_ENABLE_OPEN_IDCONNECT, false)) {
            liferay.trace("OpenIDConnectAutoLogin deployed, altough not activated. Will skip it.");
            return null;
        }

        HttpSession session = request.getSession();
        Map<String, String> userInfo = (Map<String, String>) session.getAttribute(
                LibFilter.OPENID_CONNECT_SESSION_ATTR);

        if (userInfo == null) {
            // Normal flow, apparently no current OpenID conversation
            liferay.trace("No current OpenID Connect conversation, no auto login");
            return null;
        } else if (StringUtils.isBlank(userInfo.get(USERINFO_ATTR_EMAIL))) {
            liferay.error("Unexpected: OpenID Connect UserInfo does not contain email field. " +
                    "Cannot correlate to Liferay user. UserInfo: " + userInfo);
            return null;
        } else {
            liferay.trace("Found OpenID Connect session attribute, userinfo: " + userInfo);
            String emailAddress = userInfo.get(USERINFO_ATTR_EMAIL);
            String givenName = userInfo.get(USERINFO_ATTR_FIRST_NAME);
            String familyName = userInfo.get(USERINFO_ATTR_LAST_NAME);

            long companyId = liferay.getCompanyId(request);
            String userId = liferay.createOrUpdateUser(companyId, emailAddress, givenName, familyName);
            liferay.trace("Returning credentials for userId " + userId + ", email: " + emailAddress);
            
            return new String[]{userId, UUID.randomUUID().toString(), "false"};
        }
    }
}
