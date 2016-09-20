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

    String getCurrentCompleteURL(HttpServletRequest request);

    boolean isUserLoggedIn(HttpServletRequest request);
}
