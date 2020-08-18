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
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PwdGenerator;
import com.liferay.portal.kernel.util.StringPool;
import nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


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
    public LiferaySitesConfiguration getLiferaySitesConfiguration() {
        return null;
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
    public String createOrUpdateUser(long companyId, String screenName, String emailAddress, String firstName, String lastName, List<String> groups) {
        try {
            User user = userLocalService.fetchUserByEmailAddress(companyId, emailAddress);

            if (user == null) {
                LOG.debug("No Liferay user found with email address " + emailAddress + ", will create one.");
                user = addUser(companyId, screenName, emailAddress, firstName, lastName);
            } else {
                LOG.debug("User found, updating name details with info from userinfo");
                updateUser(user, screenName, firstName, lastName);
            }
            return String.valueOf(user.getUserId());

        } catch (SystemException | PortalException e) {
            throw new RuntimeException(e);
        }
    }

    // Copied from OpenSSOAutoLogin.java
    protected User addUser(
            long companyId, String screenName, String emailAddress, String firstName, String lastName)
            throws SystemException, PortalException {

        Locale locale = LocaleUtil.getMostRelevantLocale();
        long creatorUserId = 0;
        boolean autoPassword = false;
        String password1 = PwdGenerator.getPassword();
        String password2 = password1;
        boolean autoScreenName = false;
        long facebookId = 0;
        String openId = StringPool.BLANK;
        String middleName = StringPool.BLANK;
        int prefixId = 0;
        int suffixId = 0;
        boolean male = true;
        int birthdayMonth = Calendar.JANUARY;
        int birthdayDay = 1;
        int birthdayYear = 1970;
        String jobTitle = StringPool.BLANK;
        long[] groupIds = null;
        long[] organizationIds = null;
        long[] roleIds = null;
        long[] userGroupIds = null;
        boolean sendEmail = false;
        ServiceContext serviceContext = new ServiceContext();

        User user = userLocalService.addUser(
                creatorUserId, companyId, autoPassword, password1, password2,
                autoScreenName, screenName, emailAddress, facebookId, openId,
                locale, firstName, middleName, lastName, prefixId, suffixId, male,
                birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds,
                organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

        // No password
        user.setPasswordReset(false);
        // No reminder query at first login.
        user.setReminderQueryQuestion("x");
        user.setReminderQueryAnswer("y");
        userLocalService.updateUser(user);
        return user;
    }


    private void updateUser(User user, String screenName, String firstName, String lastName) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setScreenName(screenName);

        try {
            userLocalService.updateUser(user);
        } catch (SystemException e) {
            LOG.error("Could not update user with new name attributes", e);
        }
    }
}
