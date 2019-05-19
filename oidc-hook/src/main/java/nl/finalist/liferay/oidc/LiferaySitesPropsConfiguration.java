package nl.finalist.liferay.oidc;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PrefsPropsUtil;

/**
 * Implementation of the OIDC configuration that uses Liferay's Portal properties.
 * As this implementation uses PrefsProps instead of plain Props, it is 'Virtual Instance-safe',
 * i.e. each virtual instance can have its own settings in portal-{companyId}.properties
 */
public class LiferaySitesPropsConfiguration implements LiferaySitesConfiguration {

    public String[] sitesToInclude() {
        try {
            return PrefsPropsUtil.getStringArray("openidconnect.server.name",",");
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
}
