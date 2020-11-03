package nl.finalist.liferay.oidc.providers;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import nl.finalist.liferay.oidc.dto.PersonGroupDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserInfoProvider {

    private static final Log LOG = LogFactoryUtil.getLog(UserInfoProvider.class);

    public String getEmail(Map<String, Object> userInfo) {
        return (String) userInfo.get("email");
    }

    public String getUuid(Map<String, Object> userInfo) {
        return (String) userInfo.get("uuid");
    }

    public String getFirstName(Map<String, Object> userInfo) {
        return (String) userInfo.get("firstName");
    }

    public String getLastName(Map<String, Object> userInfo) {
        return (String) userInfo.get("lastName");
    }

    public String getMiddleName(Map<String, Object> userInfo) {
        return (String) userInfo.get("middleName");
    }

    public Set<PersonGroupDto> getUserGroupIds(Map<String, Object> userInfo) {
        return ((List<Map<String, String>>) userInfo.get("groups")).stream()
                .map(this::convertMap)
                .collect(Collectors.toSet());
    }

    private PersonGroupDto convertMap(Map<String, String> map) {
        return PersonGroupDto.of(map.get("uuid"), map.get("name"));
    }

}
