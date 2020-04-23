package nl.finalist.liferay.oidc;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.model.UserGroupModel;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserGroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.persistence.UserGroupUtil;
import com.liferay.portal.service.persistence.UserUtil;
import com.liferay.portal.util.PortalUtil;
import nl.finalist.liferay.oidc.dto.PersonGroupDto;
import nl.finalist.liferay.oidc.dto.UserDto;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Liferay62Adapter implements LiferayAdapter {

    private static final Log LOG = LogFactoryUtil.getLog(Liferay62Adapter.class);

    @Override
    public OIDCConfiguration getOIDCConfiguration(long companyId) {
        return new OpenIDConnectPortalPropsConfiguration(companyId);
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
    public void error(String s) {
        LOG.error(s);
    }

    @Override
    public Long createOrUpdateUser(long companyId, UserDto userDto) {
        try {
            User user = UserLocalServiceUtil.fetchUserByUuidAndCompanyId(userDto.getUuid(), companyId);

            if (user == null) {
                LOG.debug("No Liferay user found with email address " + userDto.getUuid() + ", will create one.");
                user = addUser(companyId, userDto);
            } else {
                LOG.debug("User found, updating name details with info from userinfo");
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
                UserGroup userGroup = UserGroupLocalServiceUtil.fetchUserGroupByUuidAndCompanyId(personGroupDto.getUuid(), companyId);
                if (userGroup == null) {
                    userGroup = addNewUserGroup(companyId, userId, personGroupDto);
                } else {
                    userGroup = updateUserGroup(userGroup, personGroupDto);
                }
                groupIds.add(userGroup.getUserGroupId());
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
                    .map(UserGroupModel::getUserGroupId)
                    .collect(Collectors.toSet());
            final Set<Long> newIds = newUserGroupIds.stream()
                    .filter(id -> !oldUserGroupIds.contains(id))
                    .collect(Collectors.toSet());
            final long[] deleteIds = oldUserGroupIds.stream()
                    .filter(id -> !newIds.contains(id))
                    .mapToLong(Long::longValue)
                    .toArray();
            final long[] newIdsArray = newIds.stream().mapToLong(Long::longValue).toArray();
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
        UserLocalServiceUtil.updateUser(user);
        return user;
    }

    private void updateUser(User user, UserDto userDto) {
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setMiddleName(userDto.getMiddleName());

        try {
            UserLocalServiceUtil.updateUser(user);
        } catch (SystemException e) {
            LOG.error("Could not update user with new name attributes", e);
        }
    }

}
