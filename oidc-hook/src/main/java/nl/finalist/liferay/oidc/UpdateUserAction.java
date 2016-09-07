package nl.finalist.liferay.oidc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ClassNameLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

/**
 * Update the user right after logging in. Set the custom fields based on what the SSO has put on the session.
 */
public class UpdateUserAction extends Action {

    private static final String BONDSID_CUSTOM_FIELD_NAME = "bondsId";
    private static final String PERSOONSID_CUSTOM_FIELD_NAME = "persoonsId";
    private static final String AKF = "01";
    private static final String BG = "02";
    private static final String BOUW = "03";
    private Log LOG = LogFactoryUtil.getLog(UpdateUserAction.class);

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        try {
            User user = PortalUtil.getUser(request);
            String bondsId = (String) request.getSession().getAttribute(OpenIDConnectAutoLogin.UNION_ID_SESSION_PARAM);
            String persoonsId = (String) request.getSession()
                    .getAttribute(OpenIDConnectAutoLogin.PERSON_ID_SESSION_PARAM);

            if (Validator.isBlank(bondsId) || Validator.isBlank(persoonsId)) {
                LOG.info("PersoonsId or bondsId not set by autologin, will skip user update. " +
                        "(PersoonsId: '" + persoonsId + "', BondsId: '" + bondsId + "')");
            } else {
                setName(user, request);
                setCustomFields(user, bondsId, persoonsId);
                setRoles(user, bondsId, request);
            }
        } catch (PortalException | SystemException e) {
            LOG.error(e);
        }
    }

    private void setName(User user, HttpServletRequest request) {

        String tussenvoegsel =
                (String) request.getSession().getAttribute(OpenIDConnectAutoLogin.TUSSENVOEGSEL_SESSION_PARAM);
        String lastName = "";
        if (tussenvoegsel != null && !tussenvoegsel.isEmpty()) {
            lastName = tussenvoegsel + " ";
        }
        lastName += request.getSession().getAttribute(OpenIDConnectAutoLogin.LAST_NAME_SESSION_PARAM);
        user.setLastName(lastName);
        user.setFirstName((String) request.getSession().getAttribute(OpenIDConnectAutoLogin.FIRST_NAME_SESSION_PARAM));

        try {
			UserLocalServiceUtil.updateUser(user);
		} catch (SystemException e) {
			LOG.error("Could not update user with new name attributes", e);
		}
        
    }

    private void setCustomFields(User user, String bondsId, String persoonsId) throws SystemException, PortalException {
        if (bondsId != null && persoonsId != null) {
            long classNameId = ClassNameLocalServiceUtil.getClassNameId(User.class.getName());

            ExpandoTable defaultTable = ExpandoTableLocalServiceUtil.getDefaultTable(user.getCompanyId(), classNameId);
            ExpandoColumn expandoColumnBondId = ExpandoColumnLocalServiceUtil.getColumn(defaultTable.getTableId(),
                    BONDSID_CUSTOM_FIELD_NAME);
            ExpandoValueLocalServiceUtil.addValue(classNameId, defaultTable.getTableId(), expandoColumnBondId
                    .getColumnId(), user.getUserId(), bondsId);

            ExpandoColumn expandoColumnPersonId = ExpandoColumnLocalServiceUtil.getColumn(defaultTable.getTableId(),
                    PERSOONSID_CUSTOM_FIELD_NAME);
            ExpandoValueLocalServiceUtil.addValue(classNameId, defaultTable.getTableId(), expandoColumnPersonId
                    .getColumnId(), user.getUserId(), persoonsId);
        }
    }

    private void setRoles(User user, String bondsId, HttpServletRequest request) throws SystemException,
            PortalException {
        setBondRoles(user, bondsId);
        setLevelRoles(user, request);
    }

    private void setBondRoles(User user, String bondsId) throws PortalException, SystemException {
        if (bondsId != null) {
            Role role = null;
            switch (bondsId) {
                case AKF:
                    role = RoleLocalServiceUtil.getRole(user.getCompanyId(), "FNV ABVAKABO");
                    break;
                case BG:
                    role = RoleLocalServiceUtil.getRole(user.getCompanyId(), "FNV Bondgenoten");
                    break;
                case BOUW:
                    role = RoleLocalServiceUtil.getRole(user.getCompanyId(), "FNV Bouw");
                    break;
            }

            if (role != null) {
                RoleLocalServiceUtil.addUserRole(user.getUserId(), role.getRoleId());
            }
        }
    }

    private void setLevelRoles(User user, HttpServletRequest request) throws SystemException, PortalException {
        String isLid = (String) request.getSession().getAttribute(OpenIDConnectAutoLogin.IS_MEMBER_SESSION_PARAM);
        if (Boolean.parseBoolean(isLid)) {
            RoleLocalServiceUtil.addUserRole(user.getUserId(), RoleLocalServiceUtil.getRole(user.getCompanyId(), "FNV" +
                    " Lid").getRoleId());
        }

        String isKaderlid = (String) request.getSession().getAttribute(OpenIDConnectAutoLogin
                .IS_EXECUTIVE_MEMBER_SESSION_PARAM);
        if (Boolean.parseBoolean(isKaderlid)) {
            RoleLocalServiceUtil.addUserRole(user.getUserId(), RoleLocalServiceUtil.getRole(user.getCompanyId(), "FNV" +
                    " Kaderlid").getRoleId());
        }

        String isDienstverlenendKaderlid = (String) request.getSession().getAttribute(OpenIDConnectAutoLogin
                .IS_EXECUTIVE_MEMBER_SERVICE_SESSION_PARAM);
        if (Boolean.parseBoolean(isDienstverlenendKaderlid)) {
            RoleLocalServiceUtil.addUserRole(user.getUserId(), RoleLocalServiceUtil.getRole(user.getCompanyId(), "FNV" +
                    " Dienstverlenend kaderlid").getRoleId());
        }
    }

}
