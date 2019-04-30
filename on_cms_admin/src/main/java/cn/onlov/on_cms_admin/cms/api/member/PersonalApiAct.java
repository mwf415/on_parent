package cn.onlov.on_cms_admin.cms.api.member;


import cn.onlov.on_cms_common.cms.annotation.SignValidate;
import cn.onlov.on_cms_common.cms.api.ApiResponse;
import cn.onlov.on_cms_common.cms.api.ApiValidate;
import cn.onlov.on_cms_common.cms.api.Constants;
import cn.onlov.on_cms_common.cms.api.ResponseCode;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import cn.onlov.on_cms_common.core.entity.CmsUser;
import cn.onlov.on_cms_common.core.entity.CmsUserExt;
import cn.onlov.on_cms_common.core.manager.CmsUserExtMng;
import cn.onlov.on_cms_common.core.manager.CmsUserMng;
import cn.onlov.on_cms_common.core.web.WebErrors;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class PersonalApiAct {
	
	@SignValidate
	@RequestMapping("/personal/update")
	public void profileUpdate(String origPwd,String newPwd,
			String email,String realname,
			HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		String body="\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		CmsUser user = CmsUtils.getUser(request);
		WebErrors errors = validatePasswordSubmit(user.getId(), origPwd,
				newPwd, email, realname,request);
		if (errors.hasErrors()) {
			message=errors.getErrors().get(0).toString();
			code = ResponseCode.API_CODE_PARAM_ERROR;
		}else{
			CmsUserExt ext = user.getUserExt();
			if (ext == null) {
				ext = new CmsUserExt();
			}
			ext.setRealname(realname);
			cmsUserExtMng.update(ext, user);
			cmsUserMng.updatePwdEmail(user.getId(), newPwd, email);
			message=Constants.API_MESSAGE_SUCCESS;
			code = ResponseCode.API_CODE_CALL_SUCCESS;
		}
		ApiResponse apiResponse=new ApiResponse(request, body, message,code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/personal/check_pwd")
	public void checkPwd(String origPwd, HttpServletRequest request,
			HttpServletResponse response) {
		String body="\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors=WebErrors.create(request);
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors, origPwd);
		if(!errors.hasErrors()){
			CmsUser user = CmsUtils.getUser(request);
			Boolean pass = cmsUserMng.isPasswordValid(user.getId(), origPwd);
			JSONObject json=new JSONObject();
			json.put("pass", pass);
			body=json.toString();
			message=Constants.API_MESSAGE_SUCCESS;
			code = ResponseCode.API_CODE_CALL_SUCCESS;
		}
		ApiResponse apiResponse=new ApiResponse(request, body, message,code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private WebErrors validatePasswordSubmit(Integer id, String origPwd,
			String newPwd, String email, String realname,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (errors.ifBlank(origPwd, "origPwd", 32, true)) {
			return errors;
		}
		if (errors.ifMaxLength(newPwd, "newPwd", 32, true)) {
			return errors;
		}
		if (errors.ifMaxLength(email, "email", 100, true)) {
			return errors;
		}
		if (errors.ifMaxLength(realname, "realname", 100, true)) {
			return errors;
		}
		if (!cmsUserMng.isPasswordValid(id, origPwd)) {
			errors.addErrorString("member.origPwdInvalid");
			return errors;
		}
		return errors;
	}


	@Autowired
	private CmsUserMng cmsUserMng;
	@Autowired
	private CmsUserExtMng cmsUserExtMng;
	
}

