package nl.finalist.liferay.oidc.settings.internal.portlet.action;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.settings.portlet.action.BasePortalSettingsFormMVCActionCommand;
import com.liferay.portal.settings.web.constants.PortalSettingsPortletKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;

import nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfigurationConstants;
import nl.finalist.liferay.oidc.settings.internal.constants.PortalSettingsOidcConstants;

/**
 * Form contributor for the Virtual Instance's 'Instance Settings'.
 * To be used together with the OCD class and its *BeanDeclaration and *PidMapping.
 */
@Component(immediate = true,
    property = {
        "javax.portlet.name=" + PortalSettingsPortletKeys.PORTAL_SETTINGS,
        "mvc.command.name=/portal_settings/oidc"
    },
    service = MVCActionCommand.class)
public class OidcPortalSettingsFormContributor extends BasePortalSettingsFormMVCActionCommand {


    @Override
    protected void doValidateForm(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
    }

    @Override
	protected String getParameterNamespace() {
        return PortalSettingsOidcConstants.FORM_PARAMETER_NAMESPACE;
    }


    @Override
    public String getSettingsId() {
        return OpenIDConnectOCDConfigurationConstants.SERVICE_NAME;
    }


}