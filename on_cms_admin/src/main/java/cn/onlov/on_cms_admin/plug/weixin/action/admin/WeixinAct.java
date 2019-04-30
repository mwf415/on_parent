package cn.onlov.on_cms_admin.plug.weixin.action.admin;

import cn.onlov.on_cms_common.cms.entity.main.Content;
import cn.onlov.on_cms_common.cms.manager.main.ContentMng;
import cn.onlov.on_cms_common.cms.service.WeiXinSvc;
import cn.onlov.on_cms_common.core.Constants;
import cn.onlov.on_cms_common.core.entity.CmsSite;
import cn.onlov.on_cms_common.core.manager.CmsSiteMng;
import cn.onlov.on_cms_common.core.web.WebErrors;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import cn.onlov.on_cms_common.plug.weixin.entity.Weixin;
import cn.onlov.on_cms_common.plug.weixin.manager.WeixinMng;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WeixinAct {
	
	@RequiresPermissions("content:o_sendToWeixin")
	@RequestMapping("/content/o_sendToWeixin.do")
	public String sendToWeixin(Integer[] ids,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateCheck(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		Content[] beans = new Content[ids.length];
		for (int i = 0; i < ids.length; i++) {
			beans[i] = contentMng.findById(ids[i]);
		}
		weiXinSvc.sendTextToAllUser(beans);
		return  "redirect:v_list.do";
	}

	
	@RequiresPermissions("weixin:v_edit")
	public String add(HttpServletRequest request, ModelMap model) {
		return "weixin/add";
	}
	
	@RequiresPermissions("weixin:o_update")
	@RequestMapping("/weixin/o_save.do")
	public String save(Weixin bean,String wxAppkey,String wxAppSecret,HttpServletRequest request, ModelMap model) {
		CmsSite site=CmsUtils.getSite(request);
		bean.setSite(site);
		Map<String,String>wxMap=new HashMap<String,String>();
		wxMap.put(Constants.WEIXIN_APPKEY, wxAppkey);
		wxMap.put(Constants.WEIXIN_APPSECRET, wxAppSecret);
		siteMng.updateAttr(site.getId(), wxMap);
		manager.save(bean);
		return edit(request, model);
	}
	
	@RequiresPermissions("weixin:v_edit")
	@RequestMapping("/weixin/v_edit.do")
	public String edit(HttpServletRequest request, ModelMap model) {
		Weixin entity = manager.find(CmsUtils.getSiteId(request));
		if(entity!=null){
			model.addAttribute("site",CmsUtils.getSite(request));
			model.addAttribute("weixin",entity);
			return "weixin/edit";
		}else{
			return add(request, model);
		}
	}
	
	@RequiresPermissions("weixin:o_update")
	@RequestMapping("/weixin/o_update.do")
	public String update(Weixin bean,String wxAppkey,String wxAppSecret,
			HttpServletRequest request, ModelMap model) {
		CmsSite site=CmsUtils.getSite(request);
		Map<String,String>wxMap=new HashMap<String,String>();
		if(!StringUtils.isBlank(wxAppkey)){
			wxMap.put(Constants.WEIXIN_APPKEY, wxAppkey);
		}
		if(!StringUtils.isBlank(wxAppSecret)){
			wxMap.put(Constants.WEIXIN_APPSECRET, wxAppSecret);
		}
		siteMng.updateAttr(site.getId(), wxMap);
		manager.update(bean);
		return edit(request, model);
	}
	
	private WebErrors validateCheck(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		errors.ifEmpty(ids, "ids", true);
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}
	
	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id", true)) {
			return true;
		}
		Content entity = contentMng.findById(id);
		if (errors.ifNotExist(entity, Content.class, id, true)) {
			return true;
		}
		if (!entity.getSite().getId().equals(siteId)) {
			errors.notInSite(Content.class, id);
			return true;
		}
		return false;
	}

	@Autowired
	private WeixinMng manager;
	@Autowired
	private WeiXinSvc weiXinSvc;
	@Autowired
	private ContentMng contentMng;
	@Autowired
	private CmsSiteMng siteMng;
}
