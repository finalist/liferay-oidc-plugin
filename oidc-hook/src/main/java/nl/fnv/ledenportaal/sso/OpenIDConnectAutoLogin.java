package nl.fnv.ledenportaal.sso;

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
     * These session attributes will be retained when logging in.
     * Liferay has this 'session fixation prevention' which renews a session during login (resetting the session id)
     * which normally wipes out all current attributes.
     * Using the portal.property 'session.phishing.protected.attributes' these attributes are retained.
     */
    public static final String IS_EXECUTIVE_MEMBER_SERVICE_SESSION_PARAM = "isDienstverlenendKaderlid";
    public static final String IS_EXECUTIVE_MEMBER_SESSION_PARAM = "isKaderlid";
    public static final String IS_MEMBER_SESSION_PARAM = "isLid";
    public static final String UNION_ID_SESSION_PARAM = "bondsId";
    public static final String PERSON_ID_SESSION_PARAM = "persoonsId";
    public static final String FIRST_NAME_SESSION_PARAM = "firstName";
    public static final String LAST_NAME_SESSION_PARAM = "lastName";
    public static final String TUSSENVOEGSEL_SESSION_PARAM = "tussenvoegsel";

    /**
     * Part of the interface with SSO: the naming of the OpenID Connect UserInfo attributes
     */
    private static final String USERINFO_ATTR_IS_MEMBER = "FNVIsLid";
    private static final String USERINFO_ATTR_IS_EXECUTIVE_MEMBER = "FNVIsKaderlid";
    private static final String USERINFO_ATTR_IS_EXECUTIVE_MEMBER_SERVICE = "FNVIsDienstverlenendKaderlid";
    private static final String USERINFO_ATTR_UNION_ID = "FNVBondsId";
    private static final String USERINFO_ATTR_PERSON_ID = "FNVPersoonsId";
    private static final String USERINFO_ATTR_FIRST_NAME = "given_name";
    private static final String USERINFO_ATTR_LAST_NAME = "family_name";
    private static final String USERINFO_ATTR_TUSSENVOEGSEL = "FNVTussenvoegsel";


    @Override
    protected String[] doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (!OpenIDConnectFilter.USE_OPENID_CONNECT) {
            LOG.trace("OpenIDConnectAutoLogin deployed, altough not activated. Will skip it.");
            return null;
        }

        HttpSession session = request.getSession();
        Map<String, String> userInfo = (Map<String, String>) session.getAttribute(
                OpenIDConnectFilter.OPENID_CONNECT_SESSION_ATTR);

        if (userInfo == null) {
            // Normal flow, apparently no current OpenID conversation
            LOG.trace("No current OpenID Connect conversation, no auto login");
            return null;
        } else if (Validator.isBlank(userInfo.get("email"))) {
            LOG.error("Unexpected: OpenID Connect UserInfo does not contain email field. " +
                    "Cannot correlate to Liferay user. UserInfo: " + userInfo);
            return null;
        } else {
            LOG.trace("Found OpenID Connect session attribute, userinfo: " + userInfo);
            String emailAddress = userInfo.get("email");
            try {
                long companyId = PortalUtil.getCompanyId(request);
                User user = UserLocalServiceUtil.fetchUserByEmailAddress(companyId, emailAddress);

                if (user == null) {
                    LOG.debug("No Liferay user found with email address " + emailAddress + ", will create one.");
                    user = createUser(userInfo, companyId, request);
                }
                LOG.trace("Returning credentials for userId " + user.getUserId() + ", email: "
                        + user.getEmailAddress());

                // set needed information on the session
                session.setAttribute(IS_MEMBER_SESSION_PARAM, userInfo.get(USERINFO_ATTR_IS_MEMBER));
                session.setAttribute(IS_EXECUTIVE_MEMBER_SESSION_PARAM, userInfo.get(USERINFO_ATTR_IS_EXECUTIVE_MEMBER));
                session.setAttribute(IS_EXECUTIVE_MEMBER_SERVICE_SESSION_PARAM,
                        userInfo.get(USERINFO_ATTR_IS_EXECUTIVE_MEMBER_SERVICE));
                session.setAttribute(UNION_ID_SESSION_PARAM, userInfo.get(USERINFO_ATTR_UNION_ID));
                session.setAttribute(PERSON_ID_SESSION_PARAM, userInfo.get(USERINFO_ATTR_PERSON_ID));
                session.setAttribute(FIRST_NAME_SESSION_PARAM, userInfo.get(USERINFO_ATTR_FIRST_NAME));
                session.setAttribute(LAST_NAME_SESSION_PARAM, userInfo.get(USERINFO_ATTR_LAST_NAME));
                session.setAttribute(TUSSENVOEGSEL_SESSION_PARAM, userInfo.get(USERINFO_ATTR_TUSSENVOEGSEL));
                return new String[]{String.valueOf(user.getUserId()),
                        user.getPassword(),
                        String.valueOf(user.isPasswordEncrypted())};
            } catch (Exception e) {
                LOG.error("While obtaining Liferay user", e);
                return null;
            }
        }
    }

    private User createUser(Map<String, String> userInfo, long companyId, HttpServletRequest request) throws Exception {

        ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
        Locale locale = LocaleUtil.getDefault();

        if (themeDisplay != null) {
            // ThemeDisplay should never be null, but some users complain of this error. Cause is unknown.
            locale = themeDisplay.getLocale();
        }
        String givenName = userInfo.get(USERINFO_ATTR_FIRST_NAME);
        String familyName = "";
        String tussenvoegsel = userInfo.get(USERINFO_ATTR_TUSSENVOEGSEL);
        if (tussenvoegsel != null && !tussenvoegsel.isEmpty()) {
            familyName = tussenvoegsel + " ";
        }
        familyName += userInfo.get(USERINFO_ATTR_LAST_NAME);
        return addUser(companyId, givenName, familyName, userInfo.get("email"), locale);
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
}
