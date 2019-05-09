package cn.onlov.on_cms_admin.cms.action.member;

import cn.onlov.cms.common.cms.entity.main.ApiAccount;
import cn.onlov.cms.common.cms.entity.main.ApiUserLogin;
import cn.onlov.cms.common.cms.manager.main.ApiAccountMng;
import cn.onlov.cms.common.cms.manager.main.ApiUserLoginMng;
import cn.onlov.cms.common.cms.web.Token;
import cn.onlov.cms.common.common.util.AES128Util;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.common.web.session.SessionProvider;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.entity.CmsUser;
import cn.onlov.cms.common.core.manager.ConfigMng;
import cn.onlov.cms.common.core.manager.UnifiedUserMng;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import cn.onlov.cms.common.core.web.util.FrontUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.onlov.cms.common.cms.Constants.TPLDIR_MEMBER;
import static org.apache.shiro.web.filter.authc.FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME;

@Controller
public class CasLoginAct {
	public static final String COOKIE_ERROR_REMAINING = "_error_remaining";
	public static final String LOGIN_INPUT = "tpl.loginInput";
	public static final String LOGIN_STATUS = "tpl.loginStatus";
	public static final String TPL_INDEX = "tpl.index";


	@Token(save=true)
	@RequestMapping(value = "/login.jspx", method = RequestMethod.GET)
	public String input(String returnUrl,HttpServletRequest request,
			HttpServletResponse response,ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		String sol = site.getSolutionPath();
		Integer errorTimes=configMng.getConfigLogin().getErrorTimes();
		model.addAttribute("errorTimes", errorTimes);
		model.addAttribute("site",site);
		if(StringUtils.isBlank(returnUrl)){
			session.setAttribute(request, response, "loginSource", null);
		}
		Object source=session.getAttribute(request, "loginSource");
		if(source!=null){
			String loginSource=(String) source;
			model.addAttribute("loginSource",loginSource);
		}
		FrontUtils.frontData(request, model, site);
		return FrontUtils.getTplPath(request, sol, TPLDIR_MEMBER, LOGIN_INPUT);
	}

	@Token(remove=true)
	@RequestMapping(value = "/login.jspx", method = RequestMethod.POST)
	public String submit(String username, HttpServletRequest request,
			HttpServletResponse response,ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		String sol = site.getSolutionPath();
		Object error = request.getAttribute(DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
		FrontUtils.frontData(request, model, site);
		if (error != null) {
			model.addAttribute("error",error);
			model.addAttribute("errorRemaining", unifiedUserMng.errorRemaining(username));
		}
		session.setAttribute(request, response, "loginSource", null);
	
		return FrontUtils.getTplPath(request, sol, TPLDIR_MEMBER, LOGIN_INPUT);
	}
	
	@RequestMapping(value = "/adminLogin.jspx", method = RequestMethod.POST)
	public void adminLogin(HttpServletRequest request, 
			HttpServletResponse response,ModelMap model)  {
		CmsUser user=CmsUtils.getUser(request);
		ApiAccount apiAccount=apiAccountMng.findByDefault();
		JSONObject json=new JSONObject();
		String encryptSessionKey="";
		//登陆API后台
		if(user!=null&&apiAccount!=null){
			String aesKey=apiAccount.getAesKey();
			String sessionKey=session.getSessionId(request, response);
			apiUserLoginMng.userLogin(user.getUsername(), apiAccount.getAppId(), sessionKey,request,response);
			try {
				encryptSessionKey=
						AES128Util.encrypt(sessionKey, aesKey,apiAccount.getIvKey());
				json.put("sessionKey", encryptSessionKey);
				json.put("userName", user.getUsername());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequestMapping(value = "/adminLogout.jspx", method = RequestMethod.POST)
	public void adminLogout(String userName,
			String sessionKey,HttpServletRequest request, 
			HttpServletResponse response,ModelMap model)  {
		ApiAccount apiAccount=apiAccountMng.findByDefault();
		JSONObject json=new JSONObject();
		//登出API后台
		if(StringUtils.isNotBlank(userName)&&
				StringUtils.isNotBlank(sessionKey)&&apiAccount!=null){
			String decryptSessionKey="";
			try {
				decryptSessionKey=AES128Util.decrypt(sessionKey, 
						apiAccount.getAesKey(), apiAccount.getIvKey());
			} catch (Exception e) {
			}
			//检查是否登陆
			if(StringUtils.isNotBlank(decryptSessionKey)){
				ApiUserLogin userLogin=apiUserLoginMng.findUserLogin(userName, decryptSessionKey);
				if(userLogin!=null){
					apiUserLoginMng.userLogout(userName,null, decryptSessionKey);
				}
			}
		}
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@Autowired
	private UnifiedUserMng unifiedUserMng;
	@Autowired
	private ConfigMng configMng;
	@Autowired
	private SessionProvider session;
	@Autowired
	private ApiAccountMng apiAccountMng;
	@Autowired
	private ApiUserLoginMng apiUserLoginMng;
}
