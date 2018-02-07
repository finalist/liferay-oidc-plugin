package nl.finalist.liferay.oidc;

import nl.finalist.liferay.oidc.providers.AzureAD;
import nl.finalist.liferay.oidc.providers.UserInfoProvider;

public class ProviderFactory {

	/**
	 * Produces an instance of Provider according to the configuration.
	 */
	public static UserInfoProvider getOpenIdProvider(String providerType) {

	    UserInfoProvider openIdProvider;

		String s = providerType.toUpperCase();

		if (s.equals("AZURE")) {
			openIdProvider = new AzureAD();
		} else {
			openIdProvider = new UserInfoProvider();

		}
		
		return openIdProvider;
	}
}
