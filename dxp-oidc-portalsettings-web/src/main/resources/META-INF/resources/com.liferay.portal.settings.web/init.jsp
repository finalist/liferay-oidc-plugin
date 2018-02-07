<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
        taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ page import="com.liferay.portal.kernel.language.UnicodeLanguageUtil" %><%@
        page import="com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil" %><%@
        page import="com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator" %><%@
        page import="com.liferay.portal.kernel.settings.ParameterMapSettingsLocator" %><%@
        page import="com.liferay.portal.kernel.util.Portal" %><%@
        page import="com.liferay.portal.kernel.util.Validator" %><%@
        page import="nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfiguration" %><%@
        page import="nl.finalist.liferay.oidc.settings.internal.constants.PortalSettingsOidcConstants" %><%@
        page import="nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfigurationConstants" %>
<%@ page import="javax.portlet.ActionRequest" %>
<liferay-theme:defineObjects />
<portlet:defineObjects />