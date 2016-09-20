package nl.finalist.liferay.oidc;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.BaseAutoLogin;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.PwdGenerator;

/**
 * AutoLogin for OpenID Connect 1.0
 * This class should be used in tandem with the OpenIDConnectFilter. That filter will do the OAuth conversation and
 * set a session attribute containing the UserInfo object (the claims).
 * This AutoLogin will use the claims to find a corresponding Liferay user or create a new one if none found.
 */
public class OpenIDConnectAutoLogin extends BaseAutoLogin {

    private static final Log LOG = LogFactoryUtil.getLog(OpenIDConnectAutoLogin.class);

    /**
     * The naming of the OpenID Connect UserInfo attributes
     */

    private static final String USERINFO_ATTR_FIRST_NAME = "given_name";
    private static final String USERINFO_ATTR_LAST_NAME = "family_name";
    private static final String USERINFO_ATTR_EMAIL = "email";


    @Override
    protected String[] doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (!LibFilter.USE_OPENID_CONNECT) {
            LOG.trace("OpenIDConnectAutoLogin deployed, altough not activated. Will skip it.");
            return null;
        }

        HttpSession session = request.getSession();
        Map<String, String> userInfo = (Map<String, String>) session.getAttribute(
                LibFilter.OPENID_CONNECT_SESSION_ATTR);

        if (userInfo == null) {
            // Normal flow, apparently no current OpenID conversation
            LOG.trace("No current OpenID Connect conversation, no auto login");
            return null;
        } else if (Validator.isBlank(userInfo.get(USERINFO_ATTR_EMAIL))) {
            LOG.error("Unexpected: OpenID Connect UserInfo does not contain email field. " +
                    "Cannot correlate to Liferay user. UserInfo: " + userInfo);
            return null;
        } else {
            LOG.trace("Found OpenID Connect session attribute, userinfo: " + userInfo);
            String emailAddress = userInfo.get(USERINFO_ATTR_EMAIL);
            try {
                long companyId = PortalUtil.getCompanyId(request);
                User user = UserLocalServiceUtil.fetchUserByEmailAddress(companyId, emailAddress);

                if (user == null) {
                    LOG.debug("No Liferay user found with email address " + emailAddress + ", will create one.");
                    user = createUser(userInfo, companyId, request);
                } else {
                    LOG.debug("User found, updating name details with info from userinfo");
                    updateUser(user, userInfo);
                }

                LOG.trace("Returning credentials for userId " + user.getUserId() + ", email: "
                        + user.getEmailAddress());

                return new String[]{String.valueOf(user.getUserId()),
                        user.getPassword(),
                        String.valueOf(user.isPasswordEncrypted())};
            } catch (Exception e) {
                LOG.error("While obtaining Liferay user", e);
                return null;
            }
        }
    }

    protected User createUser(Map<String, String> userInfo, long companyId, HttpServletRequest request) throws
            Exception {

        ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
        Locale locale = LocaleUtil.getDefault();

        if (themeDisplay != null) {
            // ThemeDisplay should never be null, but some users complain of this error. Cause is unknown.
            locale = themeDisplay.getLocale();
        }
        String givenName = userInfo.get(USERINFO_ATTR_FIRST_NAME);
        String familyName = userInfo.get(USERINFO_ATTR_LAST_NAME);
        String emailAddress = userInfo.get(USERINFO_ATTR_EMAIL);
        return addUser(companyId, givenName, familyName, emailAddress, locale);
    }

    // Copied from OpenSSOAutoLogin.java
    protected User addUser(
            long companyId, String firstName, String lastName,
            String emailAddress, Locale locale) throws SystemException, PortalException {

        long creatorUserId = 0;
        boolean autoPassword = false;
        String password1 = PwdGenerator.getPassword();
        String password2 = password1;
        boolean autoScreenName = true;
        String screenName = "not_used_but_autogenerated_instead";
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

        User user = UserLocalServiceUtil.addUser(
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
        UserLocalServiceUtil.updateUser(user);
        return user;
    }

    protected void updateUser(User user, Map<String, String> userInfo) {

        user.setLastName(userInfo.get(USERINFO_ATTR_FIRST_NAME));
        user.setFirstName(userInfo.get(USERINFO_ATTR_FIRST_NAME));

        try {
            UserLocalServiceUtil.updateUser(user);
        } catch (SystemException e) {
            LOG.error("Could not update user with new name attributes", e);
        }

    }

}
