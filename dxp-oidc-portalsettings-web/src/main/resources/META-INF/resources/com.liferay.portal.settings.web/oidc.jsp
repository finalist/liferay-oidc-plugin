<%@ include file="init.jsp" %>

<%
    String ns = PortalSettingsOidcConstants.FORM_PARAMETER_NAMESPACE;

    OpenIDConnectOCDConfiguration oidcConfiguration =
            ConfigurationProviderUtil.getConfiguration(OpenIDConnectOCDConfiguration.class, new
                    ParameterMapSettingsLocator(request.getParameterMap(), ns,
                    new CompanyServiceSettingsLocator(company.getCompanyId(),
                            OpenIDConnectOCDConfigurationConstants.SERVICE_NAME)));

    boolean isEnabled = oidcConfiguration.isEnabled();
    String authorizationLocation = oidcConfiguration.authorizationLocation();
    String tokenLocation = oidcConfiguration.tokenLocation();
    String profileUri = oidcConfiguration.profileUri();
    String ssoLogoutUri = oidcConfiguration.ssoLogoutUri();
    String ssoLogoutToken = oidcConfiguration.ssoLogoutToken();
    String ssoLogoutParam = oidcConfiguration.ssoLogoutParam();
    String ssoLogoutValue = oidcConfiguration.ssoLogoutValue();
    String issuer = oidcConfiguration.issuer();
    String clientId = oidcConfiguration.clientId();
    String secret = oidcConfiguration.secret();
    String scope = oidcConfiguration.scope();
    String providerType = oidcConfiguration.providerType();
%>

<aui:fieldset>
    <aui:input name="<%= ActionRequest.ACTION_NAME %>" type="hidden" value="/portal_settings/oidc" />

    <aui:input label="enabled" name='<%= ns + "isEnabled" %>' type="checkbox" value="<%= isEnabled %>" />
    <aui:input cssClass="lfr-input-text-container" label="authorizationLocation" name='<%= ns + "authorizationLocation" %>' type="text" value="<%= authorizationLocation %>" />
    <aui:input cssClass="lfr-input-text-container" label="tokenLocation" name='<%= ns + "tokenLocation" %>' type="text" value="<%= tokenLocation %>" />
    <aui:input cssClass="lfr-input-text-container" label="profileUri" name='<%= ns + "profileUri" %>' type="text" value="<%= profileUri %>" />
    <aui:input cssClass="lfr-input-text-container" label="issuer" name='<%= ns + "issuer" %>' type="text" value="<%= issuer %>"  helpMessage="issuer-helpMessage"/>
    <aui:input cssClass="lfr-input-text-container" label="scope" name='<%= ns + "scope" %>' type="text" value="<%= scope %>" helpMessage="scope-helpMessage"/>
    <aui:input cssClass="lfr-input-text-container" label="clientId" name='<%= ns + "clientId" %>' type="text" value="<%= clientId %>" />
    <aui:input cssClass="lfr-input-text-container" label="secret" name='<%= ns + "secret" %>' type="text" value="<%= secret %>" />
    <aui:select label="providerType" name='<%= ns + "providerType" %>' value="<%= providerType %>">
        <aui:option label="generic" value="generic" />
        <aui:option label="azure" value="azure" />
    </aui:select>
    <aui:input cssClass="lfr-input-text-container" label="ssoLogoutUri" name='<%= ns + "ssoLogoutUri" %>' type="text" value="<%= ssoLogoutUri %>" />
    <aui:input cssClass="lfr-input-text-container" label="ssoLogoutToken" name='<%= ns + "ssoLogoutToken" %>'
               type="text" value="<%= ssoLogoutToken %>"/>
    <aui:input cssClass="lfr-input-text-container" label="ssoLogoutParam" name='<%= ns + "ssoLogoutParam" %>' type="text" value="<%= ssoLogoutParam %>" />
    <aui:input cssClass="lfr-input-text-container" label="ssoLogoutValue" name='<%= ns + "ssoLogoutValue" %>' type="text" value="<%= ssoLogoutValue %>" />

    <aui:button-row>
        <portlet:actionURL name="/portal_settings/oidc_delete" var="resetValuesURL">
            <portlet:param name="tabs1" value="oidc" />
        </portlet:actionURL>

        <%
            String taglibOnClick = "if (confirm('" + UnicodeLanguageUtil.get(request, "are-you-sure-you-want-to-reset-the-configured-values") + "')) {submitForm(document.hrefFm, '" + resetValuesURL.toString() + "');}";
        %>

        <aui:button cssClass="btn-lg" onClick="<%= taglibOnClick %>" value="reset-values" />
    </aui:button-row>
</aui:fieldset>