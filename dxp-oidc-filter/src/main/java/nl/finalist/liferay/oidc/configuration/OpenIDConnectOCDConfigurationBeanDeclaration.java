package nl.finalist.liferay.oidc.configuration;

import com.liferay.portal.kernel.settings.definition.ConfigurationBeanDeclaration;

public class OpenIDConnectOCDConfigurationBeanDeclaration implements ConfigurationBeanDeclaration {
    @Override
    public Class<?> getConfigurationBeanClass() {
        return OpenIDConnectOCDConfiguration.class;
    }
    

}