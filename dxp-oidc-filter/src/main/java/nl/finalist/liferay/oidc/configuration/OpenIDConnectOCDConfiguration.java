package nl.finalist.liferay.oidc.configuration;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;
import nl.finalist.liferay.oidc.OIDCConfiguration;

/**
 * OCD 'implementation' of the OIDC configuration.
 * This is the way configuration can be handled in Liferay 7.0+.
 * Together with a portal settings form contributor, this provides virtual instance specific configuration.
 */
@ExtendedObjectClassDefinition(
    category = "platform",
	scope = ExtendedObjectClassDefinition.Scope.COMPANY,
    settingsId = OpenIDConnectOCDConfigurationConstants.SERVICE_NAME)
@Meta.OCD(
    id = "nl.finalist.liferay.oidc.OpenIDConnectOCDConfiguration",

    name = "OpenID Connect",
    description = "OpenID Connect authentication configuration")
public interface OpenIDConnectOCDConfiguration extends OIDCConfiguration {

    @Meta.AD(
        deflt = "false",
        required = false
    )
    boolean isEnabled();
    
    @Meta.AD(
        required = true
    )
    String authorizationLocation();


    @Meta.AD(
        required = true
    )
    String tokenLocation();

    @Meta.AD(
        required = true
    )
    String profileUri();

    @Meta.AD(
        required = false
    )
    String ssoLogoutUri();

    @Meta.AD(
        required = false
    )
    String ssoLogoutParam();

    @Meta.AD(
        required = false
    )
    String ssoLogoutValue();

    @Meta.AD(
        required = true
    )
    String issuer();

    @Meta.AD(
        required = true
    )
    String clientId();

    @Meta.AD(
        required = true
    )
    String secret();

    @Meta.AD(
        required = true,

        deflt = "openid profile email"
    )
    String scope();

    @Meta.AD(
        required = false,
        optionValues = {"generic", "azure"},
        optionLabels = {"Generic", "Azure AD"},
        deflt = "generic"
    )
    String providerType();

}
