package cn.onlov.on_cms_admin.cms.api.front;

import cn.onlov.on_cms_admin.cms.action.front.AbstractVote;
import cn.onlov.on_cms_common.cms.api.ApiResponse;
import cn.onlov.on_cms_common.cms.api.Constants;
import cn.onlov.on_cms_common.cms.api.ResponseCode;
import cn.onlov.on_cms_common.cms.entity.assist.CmsVoteTopic;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class FrontCmsVoteApiAct extends AbstractVote {
	
	/**
	 * 调查列表API
	 * @param siteId 站点ID 非必选 默认当前站
	 * @param def 是否默认  非必选 默认全部  true默认  false 非默认 
	 * @param first 开始 非必选 默认0
	 * @param count 数量 非必选 默认10
	 */
	@RequestMapping(value = "/vote/list")
	public void cmsVoteList(Integer siteId,
			Boolean def,Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		if (siteId == null) {
			siteId = CmsUtils.getSiteId(request);
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		List<CmsVoteTopic>list=cmsVoteTopicMng.getList(
				def,siteId,first,count);
		JSONArray jsonArray=new JSONArray();
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				jsonArray.put(i, list.get(i).convertToJson(false));
			}
		}
		String body = jsonArray.toString();
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	/**
	 * 投票信息获取
	 * @param id 投票ID
	 */
	@RequestMapping(value = "/vote/get")
	public void cmsVoteGet(Integer id,
			HttpServletRequest request,HttpServletResponse response) 
					throws JSONException {
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		if (id!=null) {
			CmsVoteTopic voteTopic = cmsVoteTopicMng.findById(id);
			if (voteTopic!=null) {
				body = voteTopic.convertToJson(true).toString();
				message = Constants.API_MESSAGE_SUCCESS;
				code = ResponseCode.API_CODE_CALL_SUCCESS;
			}else{
				message = Constants.API_MESSAGE_OBJECT_NOT_FOUND;
				code =ResponseCode.API_CODE_NOT_FOUND;
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
}

