package nl.finalist.liferay.oidc;

import javax.servlet.http.HttpServletRequest;


public interface OIDCLiferay {

    String getPortalProperty(String propertyKey);
    String getPortalProperty(String propertyKey, String defaultString);
    boolean getPortalProperty(String propertyKey, boolean defaultBoolean);

    void trace(String s);
    void info(String s);
    void debug(String s);
    void warn(String s);
    void error(String s);

    String getCurrentCompleteURL(HttpServletRequest request);

    boolean isUserLoggedIn(HttpServletRequest request);

    long getCompanyId(HttpServletRequest request);

    /**
     * Create user or update if already existing. Keys to base existance on are: companyId, emailAddress.
     * GivenName and familyName are used for setting the according fields.
     *
     * @param companyId
     * @param emailAddress
     * @param firstName
     * @param lastName
     * @return userId as a String
     */
    String createOrUpdateUser(long companyId, String emailAddress, String firstName, String lastName);

}
