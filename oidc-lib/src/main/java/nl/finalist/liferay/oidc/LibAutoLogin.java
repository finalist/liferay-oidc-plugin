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
public abstract class LibAutoLogin {

    private final LiferayAdapter liferay;

    public LibAutoLogin(LiferayAdapter liferay) {
        this.liferay = liferay;
        liferay.info("Initialized LibAutoLogin with Liferay API: " + liferay.getClass().getName());
    }

    public String[] doLogin(HttpServletRequest request, HttpServletResponse response) {
    	String[] userResponse = null;
    	
        if (liferay.getPortalProperty(LibFilter.PROPKEY_ENABLE_OPEN_IDCONNECT, false)) {
        	HttpSession session = request.getSession();
            Map<String, String> userInfo = (Map<String, String>) session.getAttribute(
                    LibFilter.OPENID_CONNECT_SESSION_ATTR);
            
             if (userInfo == null) {
                 // Normal flow, apparently no current OpenID conversation
                 liferay.trace("No current OpenID Connect conversation, no auto login");
             } else if (StringUtils.isBlank(getEmail(userInfo))) {
                 liferay.error("Unexpected: OpenID Connect UserInfo does not contain email field. " +
                         "Cannot correlate to Liferay user. UserInfo: " + userInfo);
             } else {
                 liferay.trace("Found OpenID Connect session attribute, userinfo: " + userInfo);
            	 String emailAddress = getEmail(userInfo);
                 String givenName = getFirstName(userInfo);
                 String familyName = getLastName(userInfo);

                 long companyId = liferay.getCompanyId(request);
                 String userId = liferay.createOrUpdateUser(companyId, emailAddress, givenName, familyName);
                 liferay.trace("Returning credentials for userId " + userId + ", email: " + emailAddress);
                 
                 userResponse = new String[]{userId, UUID.randomUUID().toString(), "false"};
             }
        } else {
        	liferay.trace("OpenIDConnectAutoLogin deployed, altough not activated. Will skip it.");
        }
        
        return userResponse;
    }
    
    protected abstract String getEmail(Map<String, String> userInfo);
    protected abstract String getFirstName(Map<String, String> userInfo);
    protected abstract String getLastName(Map<String, String> userInfo);
}
