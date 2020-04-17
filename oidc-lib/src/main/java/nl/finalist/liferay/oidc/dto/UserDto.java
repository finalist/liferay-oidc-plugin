package nl.finalist.liferay.oidc.dto;

import java.util.Locale;

public class UserDto {

    private String uuid;
    private Locale locale;
    private long creatorUserId;
    private boolean autoPassword;
    private String password1;
    private String password2;
    private boolean autoScreenName;
    private String screenName;
    private long facebookId;
    private String openId;
    private String middleName;
    private int prefixId;
    private int suffixId;
    private String email;
    private String firstName;
    private String lastName;
    private boolean male;
    private int birthdayMonth;
    private int birthdayDay;
    private int birthdayYear;
    private String jobTitle;
    private long[] groupIds;
    private long[] organizationIds;
    private long[] roleIds;
    private long[] userGroupIds;
    private boolean sendEmail;
    private boolean passwordReset;
    private String queryQuestion;
    private String queryAnswer;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public boolean isAutoPassword() {
        return autoPassword;
    }

    public void setAutoPassword(boolean autoPassword) {
        this.autoPassword = autoPassword;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public boolean isAutoScreenName() {
        return autoScreenName;
    }

    public void setAutoScreenName(boolean autoScreenName) {
        this.autoScreenName = autoScreenName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public long getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(long facebookId) {
        this.facebookId = facebookId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public int getPrefixId() {
        return prefixId;
    }

    public void setPrefixId(int prefixId) {
        this.prefixId = prefixId;
    }

    public int getSuffixId() {
        return suffixId;
    }

    public void setSuffixId(int suffixId) {
        this.suffixId = suffixId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public int getBirthdayMonth() {
        return birthdayMonth;
    }

    public void setBirthdayMonth(int birthdayMonth) {
        this.birthdayMonth = birthdayMonth;
    }

    public int getBirthdayDay() {
        return birthdayDay;
    }

    public void setBirthdayDay(int birthdayDay) {
        this.birthdayDay = birthdayDay;
    }

    public int getBirthdayYear() {
        return birthdayYear;
    }

    public void setBirthdayYear(int birthdayYear) {
        this.birthdayYear = birthdayYear;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public long[] getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(long[] groupIds) {
        this.groupIds = groupIds;
    }

    public long[] getOrganizationIds() {
        return organizationIds;
    }

    public void setOrganizationIds(long[] organizationIds) {
        this.organizationIds = organizationIds;
    }

    public long[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(long[] roleIds) {
        this.roleIds = roleIds;
    }

    public long[] getUserGroupIds() {
        return userGroupIds;
    }

    public void setUserGroupIds(long[] userGroupIds) {
        this.userGroupIds = userGroupIds;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(boolean passwordReset) {
        this.passwordReset = passwordReset;
    }

    public String getQueryQuestion() {
        return queryQuestion;
    }

    public void setQueryQuestion(String queryQuestion) {
        this.queryQuestion = queryQuestion;
    }

    public String getQueryAnswer() {
        return queryAnswer;
    }

    public void setQueryAnswer(String queryAnswer) {
        this.queryAnswer = queryAnswer;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
