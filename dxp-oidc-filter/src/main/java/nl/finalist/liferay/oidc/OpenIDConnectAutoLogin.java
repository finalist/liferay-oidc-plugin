package nl.finalist.liferay.oidc;


import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.BaseAutoLogin;
import com.liferay.portal.kernel.service.UserLocalService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @see LibAutoLogin
 */
@Component(
    immediate = true,
    service = AutoLogin.class
    configurationPid = "nl.finalist.liferay.oidc.OpenIDConnectOCDConfiguration"
)
public class OpenIDConnectAutoLogin extends BaseAutoLogin {

    private static final Log LOG = LogFactoryUtil.getLog(OpenIDConnectAutoLogin.class);

    @Reference
    private UserLocalService _userLocalService;

    private LibAutoLogin libAutologin;

    private ConfigurationProvider _configurationProvider;

    @Reference
    protected void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        _configurationProvider = configurationProvider;
    }

    public OpenIDConnectAutoLogin() {
        super();
    }

    @Activate
    protected void activate() {
        libAutologin = new LibAutoLogin(new Liferay70Adapter(_userLocalService, _configurationProvider));
    }

    @Override
    protected String[] doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return libAutologin.doLogin(request, response);
    }

}
