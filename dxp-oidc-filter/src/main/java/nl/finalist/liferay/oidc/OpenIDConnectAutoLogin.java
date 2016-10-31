package nl.finalist.liferay.oidc;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.BaseAutoLogin;
import org.osgi.service.component.annotations.Component;

/**
 * @see LibAutoLogin
 */
@Component(immediate = true, service = AutoLogin.class)
public class OpenIDConnectAutoLogin extends BaseAutoLogin {

    private static final Log LOG = LogFactoryUtil.getLog(OpenIDConnectAutoLogin.class);


    private LibAutoLogin libAutologin;

    public OpenIDConnectAutoLogin() {
        super();
        libAutologin = new LibAutoLogin(new Liferay70());
    }

    @Override
    protected String[] doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return libAutologin.doLogin(request, response);
    }

}
