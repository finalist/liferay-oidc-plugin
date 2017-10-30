package nl.finalist.liferay.oidc.providers;

import java.util.Map;

import nl.finalist.liferay.oidc.LibAutoLogin;
import nl.finalist.liferay.oidc.LiferayAdapter;

public class GenericProvider extends LibAutoLogin {

	public static final String PROVIDER_NAME = "GENERIC";
	
	public GenericProvider(LiferayAdapter liferay) {
		super(liferay);
		liferay.debug("Instance of Generic OpenID provider");
	}
	
	@Override
	protected String getEmail(Map<String, String> userInfo) {
		return userInfo.get("email");
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
