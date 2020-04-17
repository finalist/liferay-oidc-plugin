package nl.finalist.liferay.oidc.providers;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public long[] getUserGroupIds(Map<String, Object> userInfo) {
        final List<Integer> userGroupIds = (List<Integer>) userInfo.get("userGroupIds");
        if (userGroupIds != null && !userGroupIds.isEmpty()) {
            final long[] longs = userGroupIds.stream().mapToLong(Integer::longValue).toArray();
            LOG.error(Arrays.toString(longs));
            return longs;
        }
        return null;
    }

}
