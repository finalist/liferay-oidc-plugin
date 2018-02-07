package nl.finalist.liferay.oidc;

/**
 * Our own configuration definition, portable/usable across Liferay versions.
 * Implementations of this interface can be catered towards specific versions and their
 * respective configuration implementations.
 */
public interface OIDCConfiguration {

    /**
     * Whether this OpenID Connect filter/autologin is enabled or not.
     */
    boolean isEnabled();
    
    /**
     * Location of the authorization service (request token)
     */
    String authorizationLocation();
    
    /**
     * Location of the token service (exchange code for token)
     */
    String tokenLocation();

    /**
     * UserInfo endpoint
     */
    String profileUri();
    
    /**
     * SSO logout endpoint (if offered)
     */
    String ssoLogoutUri();
    
    /**
     * Parameter name supplied to SSO logout endpoint (if offered)
     */
    String ssoLogoutParam();
    
    /**
     * Parameter value supplied to SSO logout endpoint (if offered)
     */
    String ssoLogoutValue();

    /**
     * Name of the issuer, to be confirmed with the contents of the ID token
     */
    String issuer();

    /**
     * OAuth client id.
     */
    String clientId();

    /**
     * OAuth client secret.
     */
    String secret();

    /**
     * Scope of access token to request from OIDC Provider
     */
    String scope();

    /**
     * Provider type, either Generic or Azure AD
     * @return
     */
    String providerType();
}
