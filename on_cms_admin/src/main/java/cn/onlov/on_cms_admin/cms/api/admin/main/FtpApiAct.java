package cn.onlov.on_cms_admin.cms.api.admin.main;

import cn.onlov.on_cms_common.cms.annotation.SignValidate;
import cn.onlov.on_cms_common.cms.api.ApiResponse;
import cn.onlov.on_cms_common.cms.api.ApiValidate;
import cn.onlov.on_cms_common.cms.api.Constants;
import cn.onlov.on_cms_common.cms.api.ResponseCode;
import cn.onlov.on_cms_common.common.util.StrUtils;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import cn.onlov.on_cms_common.core.entity.Ftp;
import cn.onlov.on_cms_common.core.manager.CmsLogMng;
import cn.onlov.on_cms_common.core.manager.FtpMng;
import cn.onlov.on_cms_common.core.web.WebErrors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class FtpApiAct {
	private static final Logger log = LoggerFactory.getLogger(FtpApiAct.class);
	
	/**
	 * FTP列表
	 * @param request
	 * @param response
	 */
	@RequestMapping("/ftp/list")
	public void list(HttpServletRequest request,HttpServletResponse response){
		List<Ftp> list = manager.getList();
		JSONArray jsonArray = new JSONArray();
		if (list!=null&&list.size()>0) {
			for(int i = 0 ; i<list.size();i++){
				jsonArray.put(i,list.get(i).convertToJson());
			}
		}
		String body = jsonArray.toString();
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * FTP详情
	 * @param id FTP编号
	 * @param response
	 * @param request
	 */
	@RequestMapping("/ftp/get")
	public void get(Integer id,HttpServletResponse response,HttpServletRequest request){
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		String body = "\"\"";
		Ftp bean;
		if (id!=null) {
			if (id.equals(0)) {
				bean = new Ftp();
			}else{
				bean = manager.findById(id);
			}
			if (bean!=null) {
				bean.init();
				JSONObject jsonObject = bean.convertToJson();
				body = jsonObject.toString();
				message = Constants.API_MESSAGE_SUCCESS;
				code = ResponseCode.API_CODE_CALL_SUCCESS;
			}else{
				message = Constants.API_MESSAGE_OBJECT_NOT_FOUND;
				code = ResponseCode.API_CODE_NOT_FOUND;
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * FTP新增
	 * @param bean FTP对象
	 * @param request
	 * @param response
	 */
	@SignValidate
	@RequestMapping("/ftp/save")
	public void add(Ftp bean,HttpServletRequest request,HttpServletResponse response){
		String body="\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors,
				bean.getName(),bean.getIp(),bean.getUsername(),bean.getPassword(),
				bean.getEncoding(),bean.getUrl());
		if (!errors.hasErrors()) {
			bean.init();
			bean = manager.save(bean);
			log.info("save Ftp id={}",bean.getId());
			cmsLogMng.operating(request, "ftp.log.save", "id=" + bean.getId()
			+ ";name=" + bean.getName());
			body = "{\"id\":"+"\""+bean.getId()+"\"}";
			message = Constants.API_MESSAGE_SUCCESS;
			code = ResponseCode.API_CODE_CALL_SUCCESS;
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * FTP修改
	 * @param bean FTP对象
	 * @param request
	 * @param response
	 */
	@SignValidate
	@RequestMapping("/ftp/update")
	public void Update(Ftp bean,HttpServletRequest request,HttpServletResponse response){
		String body="\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors,bean.getId(),
				bean.getName(),bean.getIp(),bean.getUsername(),
				bean.getEncoding(),bean.getUrl());
		if (!errors.hasErrors()) {
			bean = manager.update(bean);
			log.info("update Ftp id={}",bean.getId());
			cmsLogMng.operating(request, "ftp.log.update", "id=" + bean.getId()
			+ ";name=" + bean.getName());
			body = "{\"id\":"+"\""+bean.getId()+"\"}";
			message = Constants.API_MESSAGE_SUCCESS;
			code = ResponseCode.API_CODE_CALL_SUCCESS;
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * FTP删除
	 * @param ids FTP编号组
	 * @param request
	 * @param response
	 */
	@SignValidate
	@RequestMapping("/ftp/delete")
	public void delete(String ids,HttpServletRequest request,HttpServletResponse response){
		String body="\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors, ids);
		if (!errors.hasErrors()) {
			Integer[] idArray = StrUtils.getInts(ids);
			errors = validateDelete(idArray,errors);
			if (errors.hasErrors()) {
				message = errors.getErrors().get(0);
				code = ResponseCode.API_CODE_NOT_FOUND;
			}else{
				try {
					Ftp[] deleteByIds = manager.deleteByIds(idArray);
					for(int i =0;i<deleteByIds.length;i++){
						log.info("delete Ftp id={}",deleteByIds[i].getId());
						cmsLogMng.operating(request, "ftp.log.delete", "id=" + deleteByIds[i].getId()
								+ ";name=" + deleteByIds[i].getName());
					}
					message = Constants.API_MESSAGE_SUCCESS;
					code = ResponseCode.API_CODE_CALL_SUCCESS;
				}catch (Exception e) {
					message = Constants.API_MESSAGE_DATA_INTERGER_VIOLATION;
					code = ResponseCode.API_CODE_DATA_INTERGER_VIOLATION;
				}
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}

	private WebErrors validateDelete(Integer[] idArray,WebErrors errors) {
		if (idArray!=null) {
			for (int i = 0; i < idArray.length; i++) {
				vldExist(idArray[i], errors);
			}
		}
		return errors;
	}

	private boolean vldExist(Integer id, WebErrors errors) {
		if (errors.ifNull(id, "id", false)) {
			return true;
		}
		Ftp entity = manager.findById(id);
		if (errors.ifNotExist(entity, Ftp.class, id, false)) {
			return true;
		}
		return false;
	}
	
	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private FtpMng manager;
}
