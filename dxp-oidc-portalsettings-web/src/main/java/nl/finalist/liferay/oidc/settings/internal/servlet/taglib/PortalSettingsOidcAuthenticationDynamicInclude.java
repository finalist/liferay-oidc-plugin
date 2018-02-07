package nl.finalist.liferay.oidc.settings.internal.servlet.taglib;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.taglib.BaseJSPDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;

import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	immediate = true,
	property = {"portal.settings.authentication.tabs.name=OpenID Connect"},
	service = DynamicInclude.class
)
public class PortalSettingsOidcAuthenticationDynamicInclude
	extends BaseJSPDynamicInclude {

	@Override
	protected String getJspPath() {
		return "/com.liferay.portal.settings.web/oidc.jsp";
	}

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	@Reference(
		target = "(osgi.web.symbolicname=nl.finalist.liferay.oidc.portalsettings)",
		unbind = "-"
	)
	protected void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
	}

	private static final Log _log = LogFactoryUtil.getLog(PortalSettingsOidcAuthenticationDynamicInclude.class);

}