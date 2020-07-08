package nl.finalist.liferay.oidc;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PrefsPropsUtil;

/**
 * Implementation of the OIDC configuration that uses Liferay's Portal properties.
 * As this implementation uses PrefsProps instead of plain Props, it is 'Virtual Instance-safe',
 * i.e. each virtual instance can have its own settings in portal-{companyId}.properties
 */
public class OpenIDConnectPortalPropsConfiguration implements OIDCConfiguration {

    private final long companyId;

    public OpenIDConnectPortalPropsConfiguration(long companyId) {
        this.companyId = companyId;
    }

    @Override
    public boolean isEnabled() {
        try {
            return PrefsPropsUtil.getBoolean(companyId, LibFilter.PROPKEY_ENABLE_OPEN_IDCONNECT);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String authorizationLocation() {
        try {
            return PrefsPropsUtil.getString("openidconnect.authorization-location");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String tokenLocation() {
        try {
            return PrefsPropsUtil.getString("openidconnect.token-location");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String profileUri() {
        try {
            return PrefsPropsUtil.getString("openidconnect.profile-uri");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String ssoLogoutUri() {
        try {
            return PrefsPropsUtil.getString("openidconnect.sso-logout-uri", "");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String ssoLogoutParam() {
        try {
            return PrefsPropsUtil.getString("openidconnect.sso-logout-param", "");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String ssoLogoutValue() {
        try {
            return PrefsPropsUtil.getString("openidconnect.sso-logout-value", "");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String issuer() {
        try {
            return PrefsPropsUtil.getString("openidconnect.issuer");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String clientId() {
        try {
            return PrefsPropsUtil.getString("openidconnect.client-id");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String secret() {
        try {
            return PrefsPropsUtil.getString("openidconnect.secret");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String scope() {
        try {
            return PrefsPropsUtil.getString("openidconnect.scope");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String providerType() {
        try {
            return PrefsPropsUtil.getString("openidconnect.provider", "generic");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String groupClaim() {
        try {
            return PrefsPropsUtil.getString("openidconnect.groupclaim");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
}
