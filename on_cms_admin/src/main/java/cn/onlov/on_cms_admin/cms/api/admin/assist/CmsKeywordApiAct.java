package cn.onlov.on_cms_admin.cms.api.admin.assist;

import cn.onlov.on_cms_common.cms.annotation.SignValidate;
import cn.onlov.on_cms_common.cms.api.ApiResponse;
import cn.onlov.on_cms_common.cms.api.ApiValidate;
import cn.onlov.on_cms_common.cms.api.Constants;
import cn.onlov.on_cms_common.cms.api.ResponseCode;
import cn.onlov.on_cms_common.cms.entity.assist.CmsKeyword;
import cn.onlov.on_cms_common.cms.manager.assist.CmsKeywordMng;
import cn.onlov.on_cms_common.common.util.StrUtils;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import cn.onlov.on_cms_common.core.entity.CmsSite;
import cn.onlov.on_cms_common.core.manager.CmsLogMng;
import cn.onlov.on_cms_common.core.web.WebErrors;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CmsKeywordApiAct {
	private static final Logger log = LoggerFactory
			.getLogger(CmsKeywordApiAct.class);
	
	@RequestMapping("/keyword/list")
	public void list(Boolean onlyEnabled,Boolean cacheable,
			HttpServletRequest request,HttpServletResponse response){
		if (onlyEnabled==null) {
			onlyEnabled = false;
		}
		if (cacheable==null) {
			cacheable= false;
		}
		CmsSite site = CmsUtils.getSite(request);
		List<CmsKeyword> list = manager.getListBySiteId(site.getId(), onlyEnabled, cacheable);
		JSONArray jsonArray = new JSONArray();
		if (list!=null&&list.size()>0) {
			for (int i = 0; i < list.size(); i++) {
				jsonArray.put(i,list.get(i).convertToJson());
			}
		}
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		String body = jsonArray.toString();
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@RequestMapping("/keyword/get")
	public void get(Integer id,HttpServletRequest request , HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		CmsKeyword bean = new CmsKeyword();
		if (id!=null) {
			if (id.equals(0)) {
				bean = new CmsKeyword();
			}else{
				bean = manager.findById(id);
			}
			if (bean==null) {
				message = Constants.API_MESSAGE_OBJECT_NOT_FOUND;
				code = ResponseCode.API_CODE_NOT_FOUND;
			}else{
				bean.init();
				body = bean.convertToJson().toString();
				message = Constants.API_MESSAGE_SUCCESS;
				code = ResponseCode.API_CODE_CALL_SUCCESS;
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/keyword/save")
	public void save(CmsKeyword bean,HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors, bean.getName(),bean.getUrl());
		if (!errors.hasErrors()) {
			bean.init();
			bean = manager.save(bean);
			log.info("save CmsKeyword id={}", bean.getId());
			cmsLogMng.operating(request, "cmsKeyword.log.save", "id="
					+ bean.getId() + ";name=" + bean.getName());
			body = "{\"id\":"+bean.getId()+"}";
			message = Constants.API_MESSAGE_SUCCESS;
			code = ResponseCode.API_CODE_CALL_SUCCESS;
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/keyword/update")
	public void update(String ids,String names,String urls,String disableds,
			HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors, ids,names,urls,disableds);
		if (!errors.hasErrors()) {
			Integer[] idArr = StrUtils.getInts(ids);
			String[] nameArr = names.split(",");
			String[] urlArr = urls.split(",");
			Boolean[] disabledArr = strToBooleanArr(disableds);
			errors = validateUpdate(errors, idArr, nameArr, urlArr, disabledArr);
			if (errors.hasErrors()) {
				message = errors.getErrors().get(0);
				code = ResponseCode.API_CODE_PARAM_ERROR;
			}else{
				manager.updateKeywords(idArr, nameArr, urlArr, disabledArr);
				log.info("update CmsKeyword");
				cmsLogMng.operating(request, "cmsKeyword.log.update", null);
				message = Constants.API_MESSAGE_SUCCESS;
				code = ResponseCode.API_CODE_CALL_SUCCESS;
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/keyword/delete")
	public void delete(String ids,HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors, ids);
		if (!errors.hasErrors()) {
			Integer[] idArr = StrUtils.getInts(ids);
			errors = validateDelete(errors, idArr);
			if (errors.hasErrors()) {
				message = errors.getErrors().get(0);
				code = ResponseCode.API_CODE_NOT_FOUND;
			}else{
				try {
					CmsKeyword[] beans = manager.deleteByIds(idArr);
					for (CmsKeyword bean : beans) {
						log.info("delete CmsKeyword id={}", bean.getId());
						cmsLogMng.operating(request, "cmsKeyword.log.delete", "id="
								+ bean.getId() + ";name=" + bean.getName());
					}
					message = Constants.API_MESSAGE_SUCCESS;
					code = ResponseCode.API_CODE_CALL_SUCCESS;
				} catch (Exception e) {
					message = Constants.API_MESSAGE_DELETE_ERROR;
					code = ResponseCode.API_CODE_DELETE_ERROR;
				}
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private WebErrors validateDelete(WebErrors errors ,Integer[] idArr){
		if (idArr!=null) {
			for (int i = 0; i < idArr.length; i++) {
				vldExist(idArr[i], errors);
				if (errors.hasErrors()) {
					return errors;
				}
			}
		}
		return errors;
	}
	
	private Boolean[] strToBooleanArr(String str){
		Boolean[] booleans = null;
		if (StringUtils.isNotBlank(str)) {
			String[] split = str.split(",");
			booleans = new Boolean[split.length];
			for (int i = 0; i < split.length; i++) {
				booleans[i] = Boolean.parseBoolean(split[i]);
			}
		}
		return booleans;
	}
	
	private WebErrors validateUpdate(WebErrors errors ,Integer[] ids, String[] names,
			String[] urls, Boolean[] disalbeds){
		if (ids!=null) {
			for (int i = 0; i < ids.length; i++) {
				vldExist(ids[i], errors);
				if (errors.hasErrors()) {
					return errors;
				}
			}
		}
		if (errors.ifEmpty(names, "name", false)) {
			return errors;
		}
		if (errors.ifEmpty(urls, "url", false)) {
			return errors;
		}
		if (errors.ifEmpty(disalbeds, "disabled", false)) {
			return errors;
		}
		if (ids.length != names.length || ids.length != urls.length) {
			errors.addErrorString(Constants.API_MESSAGE_PARAM_ERROR);
			return errors;
		}
		return errors;
	}
	
	private boolean vldExist(Integer id , WebErrors errors) {
		if (errors.ifNull(id, "id", false)) {
			return true;
		}
		CmsKeyword entity = manager.findById(id);
		if (errors.ifNotExist(entity, CmsKeyword.class, id, false)) {
			return true;
		}
		return false;
	}
	
	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsKeywordMng manager;
}