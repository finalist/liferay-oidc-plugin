package nl.finalist.liferay.oidc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.security.auth.BaseAutoLogin;

/**
 * @see LibAutoLogin
 */
public class OpenIDConnectAutoLogin extends BaseAutoLogin {

    private static final Log LOG = LogFactoryUtil.getLog(OpenIDConnectAutoLogin.class);


    private LibAutoLogin libAutologin;

    public OpenIDConnectAutoLogin() {
        super();
        libAutologin = new LibAutoLogin(new Liferay62Adapter());
    }

    @Override
    protected String[] doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return libAutologin.doLogin(request, response);
    }


}
