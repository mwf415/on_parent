package cn.onlov.on_cms_admin.cms.action.member;

import cn.onlov.on_cms_common.common.web.RequestUtils;
import cn.onlov.on_cms_common.core.entity.CmsSite;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import cn.onlov.on_cms_common.core.web.util.FrontUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;



@Controller
public class MemberLoginAct {
	public static final String LOGIN_CSI = "tpl.loginCsi";
	public static final String TPL_SPACE = "tpl.space";

	/**
	 * 客户端包含
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/login_csi.jspx")
	public String csi(HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		// 将request中所有参数
		model.putAll(RequestUtils.getQueryParams(request));
		FrontUtils.frontData(request, model, site);
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				cn.onlov.on_cms_common.cms.Constants.TPLDIR_CSI, LOGIN_CSI);
	}

}
