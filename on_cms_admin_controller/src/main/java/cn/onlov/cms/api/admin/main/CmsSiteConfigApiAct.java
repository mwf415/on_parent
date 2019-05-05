package cn.onlov.cms.api.admin.main;

import cn.onlov.cms.common.cms.annotation.SignValidate;
import cn.onlov.cms.common.cms.api.ApiResponse;
import cn.onlov.cms.common.cms.api.ApiValidate;
import cn.onlov.cms.common.cms.api.Constants;
import cn.onlov.cms.common.cms.api.ResponseCode;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.core.entity.CmsOss;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.entity.CmsSiteCompany;
import cn.onlov.cms.common.core.manager.*;
import cn.onlov.cms.common.core.web.WebErrors;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CmsSiteConfigApiAct {
	private static final Logger log = LoggerFactory
			.getLogger(CmsSiteConfigApiAct.class);
	/**
	 * 站点设置-获取
	 * @param request
	 * @param response
	 */
	@RequestMapping("/site_config/base_get")
	public void getBaseConfig(HttpServletRequest request,HttpServletResponse response){
		CmsSite site = CmsUtils.getSite(request);
		String body = "\"\"";
		String message = Constants.API_MESSAGE_OBJECT_NOT_FOUND;
		String code = ResponseCode.API_CODE_CALL_FAIL;
		if (site!=null) {
			JSONObject json = site.convertToJson();
			body = json.toString();
			code = ResponseCode.API_CODE_CALL_SUCCESS;
			message = Constants.API_MESSAGE_SUCCESS;
		}
		ApiResponse apiResponse=new ApiResponse(request, body, message,code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 站点设置-修改
	 * @param bean
	 * @param uploadFtpId 附件FTP
	 * @param syncPageFtpId 静态页同步FTP tplIndex
	 * @param request
	 * @param response
	 */
	@SignValidate
	@RequestMapping("/site_config/base_update")
	public void updateBaseConfig(CmsSite bean,Integer ossId,Integer uploadFtpId,Integer syncPageFtpId,
			HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		CmsSite site = CmsUtils.getSite(request);
		WebErrors errors = WebErrors.create(request);
		errors= ApiValidate.validateRequiredParams(request, errors, bean.getName(),
				bean.getShortName(),bean.getDomain(),bean.getPath(),
				bean.getRelativePath(),bean.getProtocol(),bean.getDynamicSuffix(),
				bean.getStaticSuffix(),bean.getMobileStaticSync(),bean.getResouceSync(),
				bean.getPageSync(),bean.getStaticIndex(),bean.getResycleOn(),bean.getAfterCheck());
		if (!errors.hasErrors()) {
			try {
				CmsSite checkDomain = manager.findByDomain(bean.getDomain());
				if (checkDomain!=null) {
					//若已存在，修改操作需要判断id是否相同
					if (!checkDomain.getId().equals(bean.getId())) {
						errors.addErrorString(Constants.API_MESSAGE_DOMAIN_EXIST);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				errors.addErrorString(Constants.API_MESSAGE_DOMAIN_EXIST);
			}
			
			if (errors.hasErrors()) {
				message = errors.getErrors().get(0);
				if (message.equals(Constants.API_MESSAGE_DOMAIN_EXIST)) {
					code = ResponseCode.API_CODE_DOMAIN_EXIST;
				}else if (message.equals(Constants.API_MESSAGE_ACCESSPATH_EXIST)) {
					code = ResponseCode.API_CODE_ACCESSPATH_EXIST;
				}
			}else{
				String tplPath = bean.getTplPath();
				if (StringUtils.isNotBlank(bean.getTplIndex())) {
					bean.setTplIndex(tplPath+bean.getTplIndex());
				}
				bean.setId(site.getId());
				if (ossId!=null) {
					CmsOss oss = ossMng.findById(ossId);
					if (oss==null) {
						errors.addErrorString(Constants.API_MESSAGE_OBJECT_NOT_FOUND);
						code = ResponseCode.API_CODE_NOT_FOUND;
					}
				}
				if (errors.hasErrors()) {
					message = errors.getErrors().get(0);
				}else{
					bean = manager.update(bean, uploadFtpId, syncPageFtpId,ossId);
					log.info("update CmsSite success. id={}",bean.getId());
					cmsLogMng.operating(request, "cmsSiteConfig.log.updateBase", null);
					body = "{\"id\":"+"\""+bean.getId()+"\"}";
					message = Constants.API_MESSAGE_SUCCESS;
					code = ResponseCode.API_CODE_CALL_SUCCESS;
				}
			}
		}
		ApiResponse apiResponse=new ApiResponse(request, body, message,code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@RequestMapping("/site_config/company_get")
	public void companyGet(HttpServletResponse response,HttpServletRequest request){
		CmsSite site = CmsUtils.getSite(request);
		CmsSiteCompany company = site.getSiteCompany();
		String body = company.convertToJson().toString();
		String message =Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		ApiResponse apiResponse=new ApiResponse(request, body, message,code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/site_config/company_update")
	public void companyUpdate(CmsSiteCompany bean,HttpServletResponse response,HttpServletRequest request){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		errors = ApiValidate.validateRequiredParams(request, errors, bean.getName(),bean.getScale(),
				bean.getNature(),bean.getIndustry());
		if (!errors.hasErrors()) {
			if (!bean.getId().equals(site.getId())) {
				message = "error.notInSite";
				code = ResponseCode.API_CODE_CALL_FAIL;
			}else{
				bean = siteCompanyMng.update(bean);
				log.info("update CmsSite success. id={}", site.getId());
				cmsLogMng.operating(request, "cmsSiteConfig.log.updateBase", null);
				body = "{\"id\":"+bean.getId()+"}";
				message = Constants.API_MESSAGE_SUCCESS;
				code = ResponseCode.API_CODE_CALL_SUCCESS;
			}
		}
		ApiResponse apiResponse=new ApiResponse(request, body, message,code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@Autowired
	private CmsConfigMng configMng;
	@Autowired
	private CmsSiteCompanyMng siteCompanyMng;
	@Autowired
	private CmsOssMng ossMng;
	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsSiteMng manager;
}
