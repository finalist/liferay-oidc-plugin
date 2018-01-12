package nl.finalist.liferay.oidc;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import nl.finalist.liferay.oidc.LibFilter.FilterResult;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

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
     * Filter the request. The first time this filter gets hit, it will redirect to the OP.
     * Second time it will expect a code and state param to be set, and will exchange the code for an access token.
     * Then it will request the UserInfo given the access token.
     * Result: the OpenID Connect 1.0 flow.
     *
     * @param request the http request
     * @param response the http response
     * @param filterChain the filterchain
     * @throws Exception according to interface.
     */
    protected void processFilter(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception {

		String pathInfo = request.getPathInfo();

		if (Validator.isNotNull(pathInfo)) {
			if (pathInfo.contains("/portal/login")) {

				// Process OpenID Connect Login flow
				
				FilterResult filterResult = libFilter.processFilter(request, response, filterChain);
		        if (filterResult == FilterResult.CONTINUE_CHAIN) { 
		        	processFilter(getClass(), request, response, filterChain);
		        }
		        
			} 
			else
			if (pathInfo.contains("/portal/logout")) {
				
		        // Based on CAS Filter implementation:
		        // If Portal Logout URL is requested, redirect to OIDC Logout resource instead to globally logout.
		        // From there, the request should be redirected back to the Liferay Logout URL to locally logout.
				
				request.getSession().invalidate();

				String logoutUrl = libFilter.SSO_LOGOUT_URI;
				
				if (null != logoutUrl && logoutUrl.length() > 0) {
					// build logout URL and append params if present
					String logoutUrlParamName = libFilter.SSO_LOGOUT_PARAM;
					String logoutUrlParamValue = libFilter.SSO_LOGOUT_VALUE;
					if (StringUtils.isNotEmpty(logoutUrlParamName) && StringUtils.isNotEmpty(logoutUrlParamValue)) {
						logoutUrl = HttpUtil.setParameter(logoutUrl, logoutUrlParamName, logoutUrlParamValue);
					}
					
					LOG.info("On " + request.getRequestURL() + " [" + pathInfo + "] redirect to logoutUrl: " + logoutUrl);
					try {
						response.sendRedirect(logoutUrl);
					} catch (IOException e) {
						LOG.error("Redirect failed from [" + request.getRequestURL() + "] to [" + logoutUrl + "]: " + e, e);
					}
					return;
				} else {
					LOG.info("On " + request.getRequestURL() + " [" + pathInfo + "] DO NOT redirect. -- logoutUrl: " + logoutUrl);
				}
				
			}
		}
    }

}
