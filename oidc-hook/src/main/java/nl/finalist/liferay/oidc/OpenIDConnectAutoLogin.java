package nl.finalist.liferay.oidc;

import com.liferay.portal.security.auth.BaseAutoLogin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @see LibAutoLogin
 */
public class OpenIDConnectAutoLogin extends BaseAutoLogin {

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
