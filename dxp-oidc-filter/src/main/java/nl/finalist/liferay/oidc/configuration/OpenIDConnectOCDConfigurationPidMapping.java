package nl.finalist.liferay.oidc.configuration;

import com.liferay.portal.kernel.settings.definition.ConfigurationPidMapping;

import org.osgi.service.component.annotations.Component;

/**
 * Configuration PID mapping for the OIDC Configuration
 */
@Component
public class OpenIDConnectOCDConfigurationPidMapping implements ConfigurationPidMapping {

    @Override
    public Class<?> getConfigurationBeanClass() {
        return OpenIDConnectOCDConfiguration.class;
    }

    @Override
    public String getConfigurationPid() {
        return OpenIDConnectOCDConfigurationConstants.SERVICE_NAME;
    }
}
