package nl.finalist.liferay.oidc;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.finalist.liferay.oidc.LibFilter.FilterResult;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;

/**
 * Servlet filter that initiates OpenID Connect logins, and handles the resulting flow, until and including the
 * UserInfo request. It saves the UserInfo in a session attribute, to be examined by an AutoLogin.
 */
public class OpenIDConnectFilter extends BaseFilter {

    private static final Log LOG = LogFactoryUtil.getLog(LibFilter.class);

    @Override
    protected Log getLog() {
        return LOG;
    }

    private LibFilter libFilter;

    @Override
    public void init(FilterConfig filterConfig) {
        libFilter = new LibFilter(new Liferay62Adapter());

        super.init(filterConfig);
    }

    /**
     * Filter the request. 
     * <br><br>LOGIN:<br> 
     * The first time this filter gets hit, it will redirect to the OP.
     * Second time it will expect a code and state param to be set, and will exchange the code for an access token.
     * Then it will request the UserInfo given the access token.
     * <br>--
     * Result: the OpenID Connect 1.0 flow.
     * <br><br>LOGOUT:<br>
     * When the filter is hit and according values for SSO logout are set, it will redirect to the OP logout resource.
     * From there the request should be redirected "back" to a public portal page or the public portal home page. 
     *
     * @param request the http request
     * @param response the http response
     * @param filterChain the filterchain
     * @throws Exception according to interface.
     */
    protected void processFilter(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception {

        FilterResult filterResult = libFilter.processFilter(request, response, filterChain);
        if (filterResult == FilterResult.CONTINUE_CHAIN) { 
        	processFilter(getClass(), request, response, filterChain);
        }
    }

}
