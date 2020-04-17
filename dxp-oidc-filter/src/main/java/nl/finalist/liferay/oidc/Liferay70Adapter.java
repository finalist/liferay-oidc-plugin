package nl.finalist.liferay.oidc;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.persistence.UserUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfiguration;
import nl.finalist.liferay.oidc.dto.UserDto;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public class Liferay70Adapter implements LiferayAdapter {

    private static final Log LOG = LogFactoryUtil.getLog(Liferay70Adapter.class);

    private UserLocalService userLocalService;
    private ConfigurationProvider configurationProvider;


    public Liferay70Adapter(UserLocalService userLocalService, ConfigurationProvider
            configurationProvider) {
        this.userLocalService = userLocalService;
        this.configurationProvider = configurationProvider;
    }

    public OIDCConfiguration getOIDCConfiguration(long companyId) {
        try {
            return configurationProvider.getCompanyConfiguration(OpenIDConnectOCDConfiguration.class, companyId);
        } catch (ConfigurationException e) {
            throw new SystemException(e);
        }
    }

    @Override
    public void trace(String s) {
        LOG.trace(s);
    }

    @Override
    public void info(String s) {
        LOG.info(s);
    }

    @Override
    public void debug(String s) {
        LOG.debug(s);
    }

    @Override
    public void warn(String s) {
        LOG.warn(s);
    }

    @Override
    public void error(String s) {
        LOG.error(s);
    }

    @Override
    public String getCurrentCompleteURL(HttpServletRequest request) {
        return PortalUtil.getCurrentCompleteURL(request);
    }

    @Override
    public boolean isUserLoggedIn(HttpServletRequest request) {
        try {
            return PortalUtil.getUser(request) != null;
        } catch (PortalException | SystemException e) {
            return false;
        }
    }

    @Override
    public long getCompanyId(HttpServletRequest request) {
        return PortalUtil.getCompanyId(request);
    }


    @Override
    public String createOrUpdateUser(long companyId, UserDto userDto) {
        try {
            User user = userLocalService.fetchUserByUuidAndCompanyId(userDto.getUuid(), companyId);
            LOG.error(userDto);
            if (user == null) {
                LOG.debug("No Liferay user found with email address " + userDto.getUuid() + ", will create one.");
                user = addUser(companyId, userDto);
            } else {
                LOG.debug("User found, updating name details with info from userinfo");
                updateUser(user, userDto);
            }
            return String.valueOf(user.getUserId());

        } catch (SystemException | PortalException e) {
            throw new RuntimeException(e);
        }
    }

    // Copied from OpenSSOAutoLogin.java
    protected User addUser(long companyId, UserDto userDto) throws SystemException, PortalException {
        ServiceContext serviceContext = new ServiceContext();
        User user = UserLocalServiceUtil.addUser(
                userDto.getCreatorUserId(),
                companyId,
                userDto.isAutoPassword(),
                userDto.getPassword1(),
                userDto.getPassword2(),
                userDto.isAutoScreenName(),
                userDto.getScreenName(),
                userDto.getEmail(),
                userDto.getFacebookId(),
                userDto.getOpenId(),
                userDto.getLocale(),
                userDto.getFirstName(),
                userDto.getMiddleName(),
                userDto.getLastName(),
                userDto.getPrefixId(),
                userDto.getSuffixId(),
                userDto.isMale(),
                userDto.getBirthdayMonth(),
                userDto.getBirthdayDay(),
                userDto.getBirthdayYear(),
                userDto.getJobTitle(),
                userDto.getGroupIds(),
                userDto.getOrganizationIds(),
                userDto.getRoleIds(),
                userDto.getUserGroupIds(),
                userDto.isSendEmail(),
                serviceContext
        );
        user.setUuid(userDto.getUuid());
        user.setPasswordReset(userDto.isPasswordReset());
        user.setReminderQueryQuestion(userDto.getQueryQuestion());
        user.setReminderQueryAnswer(userDto.getQueryAnswer());
        userLocalService.updateUser(user);
        return user;
    }

    private void updateUser(User user, UserDto userDto) {
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setMiddleName(userDto.getMiddleName());
        updateUserGroupIds(user, userDto);
        try {
            userLocalService.updateUser(user);
        } catch (SystemException e) {
            LOG.error("Could not update user with new name attributes", e);
        }
    }

    private void updateUserGroupIds(User user, UserDto userDto) {
        try {
            final Set<Long> oldUserGroupId = Arrays.stream(user.getUserGroupIds()).boxed().collect(Collectors.toSet());
            final Set<Long> newUserGroupId = Arrays.stream(userDto.getUserGroupIds()).boxed().collect(Collectors.toSet());
            final Set<Long> newIds = newUserGroupId.stream().filter(id -> !oldUserGroupId.contains(id)).collect(Collectors.toSet());
            final Set<Long> deleteIds = oldUserGroupId.stream().filter(id -> !newUserGroupId.contains(id)).collect(Collectors.toSet());
            newIds.forEach(id -> addGroup(user.getUserId(), id));
            deleteIds.forEach(id -> deleteGroup(user.getUserId(), id));
        } catch (SystemException e) {
            LOG.error(e.getMessage());
        }
    }

    private void deleteGroup(long userId, Long groupId) {
        try {
            UserUtil.removeUserGroup(userId, groupId);
        } catch (SystemException e) {
            LOG.error(e.getMessage());
        }
    }

    private void addGroup(long userId, Long groupId) {
        try {
            UserUtil.addUserGroup(userId, groupId);
        } catch (SystemException e) {
            LOG.error(e.getMessage());
        }
    }

}
