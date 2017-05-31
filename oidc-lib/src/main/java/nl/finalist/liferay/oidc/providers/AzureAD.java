package nl.finalist.liferay.oidc.providers;

import java.util.Map;

import nl.finalist.liferay.oidc.LibAutoLogin;
import nl.finalist.liferay.oidc.LiferayAdapter;

public class AzureAD extends LibAutoLogin {

	public static final String PROVIDER_NAME = "AZURE";
	
	public AzureAD(LiferayAdapter liferay) {
		super(liferay);
		liferay.debug("Instance of AzureAD OpenID provider");
	}

	@Override
	protected String getEmail(Map<String, String> userInfo) {
		return userInfo.get("unique_name");
	}

	@Override
	protected String getFirstName(Map<String, String> userInfo) {
		return userInfo.get("given_name");
	}

	@Override
	protected String getLastName(Map<String, String> userInfo) {
		return userInfo.get("family_name");
	}
}
