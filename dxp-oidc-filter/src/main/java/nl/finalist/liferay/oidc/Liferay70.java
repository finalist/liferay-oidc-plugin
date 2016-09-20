package nl.finalist.liferay.oidc;

import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PortalUtil;

public class Liferay70 implements OIDCLiferay {

    private static final Log LOG = LogFactoryUtil.getLog(Liferay70.class);


    @Override
    public String getPortalProperty(String propertyKey) {
        return null;
    }

    @Override
    public String getPortalProperty(String propertyKey, String defaultString) {
        return null;
    }

    @Override
    public boolean getPortalProperty(String propertyKey, boolean defaultBoolean) {
        return false;
    }

    @Override
    public void trace(String s) {
        LOG.trace(s);
    }

    @Override
    public void info(String s) {
        LOG.info(s);
    }

    @Override
    public void debug(String s) {
        LOG.debug(s);
    }

    @Override
    public void warn(String s) {
        LOG.warn(s);
    }

    @Override
    public String getCurrentCompleteURL(HttpServletRequest request) {
        return PortalUtil.getCurrentCompleteURL(request);
    }

    @Override
    public boolean isUserLoggedIn(HttpServletRequest request) {
        try {
            return PortalUtil.getUser(request) != null;
        } catch (PortalException | SystemException e) {
            return false;
        }
    }
}
