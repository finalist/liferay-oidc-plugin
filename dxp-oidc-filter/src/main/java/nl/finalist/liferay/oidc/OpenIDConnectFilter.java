package nl.finalist.liferay.oidc;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.BaseFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import nl.finalist.liferay.oidc.LibFilter.FilterResult;
import nl.finalist.liferay.oidc.configuration.OpenIDConnectOCDConfiguration;

@Component(
		immediate = true,
        property = {
                "dispatcher=FORWARD",
                "dispatcher=REQUEST",
                "servlet-context-name=",
                "servlet-filter-name=SSO OpenID Connect Filter",
                "url-pattern=/c/portal/login",
                "url-pattern=/c/portal/logout"
        },
        service = Filter.class,
        configurationPid = "nl.finalist.liferay.oidc.OpenIDConnectOCDConfiguration"
)
public class OpenIDConnectFilter extends BaseFilter {

    private static final Log LOG = LogFactoryUtil.getLog(OpenIDConnectFilter.class);
    private LibFilter libFilter;
    
    @Reference
    private UserLocalService _userLocalService;

    private volatile OpenIDConnectOCDConfiguration _configuration;

    private ConfigurationProvider _configurationProvider;

    @Reference
    protected void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        _configurationProvider = configurationProvider;
    }

    @Activate
    @Modified
    protected void activate() {
        libFilter = new LibFilter(new Liferay70Adapter(_userLocalService, _configurationProvider));
    }

    @Override
    protected Log getLog() {
        return LOG;
    }

    @Override
    protected void processFilter(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws Exception {

        FilterResult filterResult = libFilter.processFilter(request, response, filterChain);
        if (filterResult == FilterResult.CONTINUE_CHAIN) {
        	processFilter(getClass().getName(), request, response, filterChain);
        }
    }


}