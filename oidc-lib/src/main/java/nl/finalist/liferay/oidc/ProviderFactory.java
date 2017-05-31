package nl.finalist.liferay.oidc;

import org.apache.commons.lang3.StringUtils;

import nl.finalist.liferay.oidc.providers.AzureAD;
import nl.finalist.liferay.oidc.providers.GenericProvider;

public class ProviderFactory {
	private static final String PROPKEY_PROVIDER = "openidconnect.provider";
	
	/**
	 * Produces an instance of LibAutoLogin according to the provider property found in the portal_ext file.
	 * 
	 * @param liferay The LiferayAdapter object that fit the Liferay version of the instance (7 or 6.2)
	 * @return The corresponding OpenID provider that is specified in the portal_ext file, the Generic provider otherwise
	 */
	public static LibAutoLogin getOpenIdProvider(LiferayAdapter liferay) {
		LibAutoLogin openIdProvider = null;
		String providerName = liferay.getPortalProperty(PROPKEY_PROVIDER, StringUtils.EMPTY);
		
		switch(providerName.toUpperCase()) {
			case AzureAD.PROVIDER_NAME:
				openIdProvider = new AzureAD(liferay);
				break;
			default:
				openIdProvider = new GenericProvider(liferay);
				break;
		}
		
		return openIdProvider;
	}
}
