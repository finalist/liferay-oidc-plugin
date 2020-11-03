package nl.finalist.liferay.oidc;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.persistence.UserGroupUtil;
import com.liferay.portal.kernel.service.persistence.UserUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfiguration;
import nl.finalist.liferay.oidc.dto.PersonGroupDto;
import nl.finalist.liferay.oidc.dto.UserDto;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
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
    public Long createOrUpdateUser(long companyId, UserDto userDto) {
        try {
            User user = userLocalService.fetchUserByUuidAndCompanyId(userDto.getUuid(), companyId);
            if (user == null) {
                user = addUser(companyId, userDto);
            } else {
                updateUser(user, userDto);
            }
            return user.getUserId();

        } catch (SystemException | PortalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Long> createOrUpdateUserGroup(long companyId, long userId, Set<PersonGroupDto> personGroupDtos) {
        Set<Long> groupIds = new HashSet<>();
        for (PersonGroupDto personGroupDto : personGroupDtos) {
            try {
                final UserGroup userGroup = UserGroupLocalServiceUtil.fetchUserGroupByUuidAndCompanyId(personGroupDto.getUuid(), companyId);
                if (userGroup == null) {
                    groupIds.add(addNewUserGroup(companyId, userId, personGroupDto).getUserGroupId());
                } else {
                    groupIds.add(updateUserGroup(userGroup, personGroupDto).getUserGroupId());
                }
            } catch (SystemException e) {
                LOG.error(e.getMessage());
            }
        }
        return groupIds;
    }

    private UserGroup updateUserGroup(UserGroup userGroup, PersonGroupDto personGroupDto) {
        try {
            userGroup.setName(personGroupDto.getName());
            return UserGroupLocalServiceUtil.updateUserGroup(userGroup);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private UserGroup addNewUserGroup(long companyId, long userId, PersonGroupDto personGroupDto) {
        try {
            final UserGroup userGroup = UserGroupLocalServiceUtil.addUserGroup(userId, companyId, personGroupDto.getName(), null, null);
            userGroup.setUuid(personGroupDto.getUuid());
            return UserGroupLocalServiceUtil.updateUserGroup(userGroup);
        } catch (PortalException | SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUserInUserGroup(Long userId, Set<Long> newUserGroupIds) {
        try {
            final Set<Long> oldUserGroupIds = UserGroupLocalServiceUtil.getUserUserGroups(userId).stream()
                    .map(getUserGroupLongFunction())
                    .collect(Collectors.toSet());
            final long[] newIdsArray = newUserGroupIds.stream()
                    .filter(id -> !oldUserGroupIds.contains(id))
                    .mapToLong(Long::longValue)
                    .toArray();
            final long[] deleteIds = oldUserGroupIds.stream()
                    .filter(id -> !newUserGroupIds.contains(id))
                    .mapToLong(Long::longValue)
                    .toArray();
            if (newIdsArray != null && newIdsArray.length > 0) {
                UserUtil.addUserGroups(userId, newIdsArray);
                UserGroupUtil.clearCache();
            }
            if (deleteIds != null && deleteIds.length > 0) {
                UserUtil.removeUserGroups(userId, deleteIds);
                UserGroupUtil.clearCache();
            }
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private Function<UserGroup, Long> getUserGroupLongFunction() {
        return userGroup -> {
            try {
                return userGroup.getUserGroupId();
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        };
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
                null,
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
        user.setEmailAddress(userDto.getEmail());

        try {
            userLocalService.updateUser(user);
        } catch (SystemException e) {
            LOG.error("Could not update user with new name attributes", e);
        }
    }


}
