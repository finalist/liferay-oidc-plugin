package nl.finalist.liferay.oidc.providers;

import java.util.Map;

public class AzureAD extends UserInfoProvider {

	@Override
	public String getEmail(Map<String, Object> userInfo) {
		return (String) userInfo.get("unique_name");
	}

	@Override
	public String getFirstName(Map<String, Object> userInfo) {
		return (String) userInfo.get("firstName");
	}

	@Override
	public String getLastName(Map<String, Object> userInfo) {
		return (String) userInfo.get("lastName");
	}
}
