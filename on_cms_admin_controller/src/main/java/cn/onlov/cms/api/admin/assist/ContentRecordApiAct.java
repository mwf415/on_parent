package cn.onlov.cms.api.admin.assist;

import cn.onlov.cms.common.cms.annotation.SignValidate;
import cn.onlov.cms.common.cms.api.ApiResponse;
import cn.onlov.cms.common.cms.api.Constants;
import cn.onlov.cms.common.cms.api.ResponseCode;
import cn.onlov.cms.common.cms.entity.main.ContentRecord;
import cn.onlov.cms.common.cms.manager.main.ContentRecordMng;
import cn.onlov.cms.common.common.util.StrUtils;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.web.WebErrors;
import cn.onlov.cms.common.core.web.util.CmsUtils;
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
public class ContentRecordApiAct {
	private static final Logger log = LoggerFactory.getLogger(ContentRecordApiAct.class);
	
	@RequestMapping("/content/record/list")
	public void list(Integer contentId,HttpServletRequest request,HttpServletResponse response){
		List<ContentRecord> list = manager.getListByContentId(contentId);
		JSONArray jsonArray = new JSONArray();
		if (list!=null&&list.size()>0) {
			for(int i = 0 ; i <list.size(); i++){
				jsonArray.put(i,list.get(i).convertToJson());
			}
		}
		String body = jsonArray.toString();
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/content/record/delete")
	public void delete(String ids,HttpServletRequest request, HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		CmsSite site = CmsUtils.getSite(request);
		WebErrors errors = WebErrors.create(request);
		if (!errors.hasErrors()) {
			Long[] idArr = StrUtils.getLongs(ids);
			for (Long id : idArr) {
				vldExist(id, site.getId(), errors);
			}
			if (errors.hasErrors()) {
				message= errors.getErrors().get(0);
				code = ResponseCode.API_CODE_PARAM_ERROR;
			}else{
				try {
					ContentRecord[] beans = manager.deleteByIds(idArr);
					for (ContentRecord bean : beans) {
						log.info("delete ContentRecord id={}",bean.getId());
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
	
	private boolean vldExist(Long id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id", false)) {
			return true;
		}
		ContentRecord entity = manager.findById(id);
		if (errors.ifNotExist(entity, ContentRecord.class, id, false)) {
			return true;
		}
		return false;
	}
	
	
	@Autowired
	private ContentRecordMng manager;
}
