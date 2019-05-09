package cn.onlov.on_cms_admin.cms.action.front;


import cn.onlov.cms.common.cms.entity.main.Content;
import cn.onlov.cms.common.cms.manager.main.ContentBuyMng;
import cn.onlov.cms.common.cms.manager.main.ContentMng;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.core.entity.CmsConfig;
import cn.onlov.cms.common.core.entity.CmsGroup;
import cn.onlov.cms.common.core.entity.CmsUser;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import cn.onlov.cms.common.core.web.util.FrontUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * 静态页检查浏览权限
 */
@Controller
public class StaticPageCheckPermAct {
	
	public static final String GROUP_FORBIDDEN = "login.groupAccessForbidden";
	
	private static final Logger log = LoggerFactory
			.getLogger(StaticPageCheckPermAct.class);
	
	@RequestMapping(value = "/page_checkperm.jspx")
	public void checkPerm(Integer contentId, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws JSONException {
		Content content = contentMng.findById(contentId);
		String result="1";
		if (content == null) {
			log.debug("Content id not found: {}", contentId);
			result="2";
		}
		CmsUser user = CmsUtils.getUser(request);
		CmsConfig config=CmsUtils.getSite(request).getConfig();
		Boolean preview=config.getConfigAttr().getPreview();
		Set<CmsGroup> groups = content.getViewGroupsExt();
		int len = groups.size();
		// 需要浏览权限
		if (len != 0) {
			// 没有登录
			if (user == null) {
				result="3";
			}else{
				// 已经登录但没有权限
				Integer gid = user.getGroup().getId();
				boolean right = false;
				for (CmsGroup group : groups) {
					if (group.getId().equals(gid)) {
						right = true;
						break;
					}
				}
				//无权限且不支持预览
				if (!right&&!preview) {
					result="4";
				}
				//无权限支持预览
				if(!right&&preview){
					result="6";
				}
			}
		}
		//收费模式，检查是否已购买
		if(content.getCharge()){
			if (user == null) {
				result="3";
			}else{
				if(!content.getUser().equals(user)){
					boolean hasBuy=contentBuyMng.hasBuyContent(user.getId(), contentId);
					if(!hasBuy){
						result="5";
					}
				}
			}
		}
		ResponseUtils.renderJson(response, result);
	}
	
	@RequestMapping(value = "/user_no_login.jspx")
	public String userNoLogin(HttpServletRequest request,HttpServletResponse response, ModelMap model){
		return FrontUtils.showLogin(request, model, CmsUtils.getSite(request));
	}
	
	@RequestMapping(value = "/group_forbidden.jspx")
	public String groupForbidden(HttpServletRequest request,HttpServletResponse response, ModelMap model){
		CmsUser user=CmsUtils.getUser(request);
		if(user!=null){
			return FrontUtils.showMessage(request, model, GROUP_FORBIDDEN,user.getGroup().getName());	
		}else{
			return userNoLogin(request, response, model);
		}
	}
	
	@Autowired
	private ContentMng contentMng;
	@Autowired
	private ContentBuyMng contentBuyMng;
}
