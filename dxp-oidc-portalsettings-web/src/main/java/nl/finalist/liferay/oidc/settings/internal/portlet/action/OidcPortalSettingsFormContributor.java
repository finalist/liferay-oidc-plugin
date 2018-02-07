package nl.finalist.liferay.oidc.settings.internal.portlet.action;

import com.liferay.portal.settings.web.portlet.action.PortalSettingsFormContributor;

import java.util.Optional;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import org.osgi.service.component.annotations.Component;

import nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfigurationConstants;
import nl.finalist.liferay.oidc.settings.internal.constants.PortalSettingsOidcConstants;

/**
 * Form contributor for the Virtual Instance's 'Instance Settings'.
 * To be used together with the OCD class and its *BeanDeclaration and *PidMapping.
 */
@Component(immediate = true, service = PortalSettingsFormContributor.class)
public class OidcPortalSettingsFormContributor implements PortalSettingsFormContributor {

    @Override
    public Optional<String> getDeleteMVCActionCommandNameOptional() {
        return Optional.of("/portal_settings/oidc_delete");
    }

    @Override
    public String getParameterNamespace() {
        return PortalSettingsOidcConstants.FORM_PARAMETER_NAMESPACE;
    }

    @Override
    public Optional<String> getSaveMVCActionCommandNameOptional() {
        return Optional.of("/portal_settings/oidc");
    }

    @Override
    public String getSettingsId() {
        return OpenIDConnectOCDConfigurationConstants.SERVICE_NAME;
    }

    @Override
    public void validateForm(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws PortletException {
        // No specific validation.
    }

}