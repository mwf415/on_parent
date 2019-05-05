package cn.onlov.cms.api.admin.assist;

import cn.onlov.cms.common.cms.api.ApiResponse;
import cn.onlov.cms.common.cms.api.Constants;
import cn.onlov.cms.common.cms.api.ResponseCode;
import cn.onlov.cms.common.cms.entity.assist.CmsConfigContentCharge;
import cn.onlov.cms.common.cms.entity.main.ContentBuy;
import cn.onlov.cms.common.cms.entity.main.ContentCharge;
import cn.onlov.cms.common.cms.manager.assist.CmsConfigContentChargeMng;
import cn.onlov.cms.common.cms.manager.main.ContentBuyMng;
import cn.onlov.cms.common.cms.manager.main.ContentChargeMng;
import cn.onlov.cms.common.common.page.Pagination;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.core.entity.CmsUser;
import cn.onlov.cms.common.core.entity.CmsUserAccount;
import cn.onlov.cms.common.core.manager.CmsUserAccountMng;
import cn.onlov.cms.common.core.manager.CmsUserMng;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
public class CmsContentChargeApiAct {
	@SuppressWarnings("unchecked")
	@RequestMapping("/contentBuy/charge_list")
	public void contentBuyList(String contentTitle,String author,
			Date buyTimeBegin,Date buyTimeEnd,Integer orderBy,Integer pageNo,Integer pageSize,
			HttpServletRequest request,HttpServletResponse response){
		if (orderBy==null) {
			orderBy = 1;
		}
		if (pageNo==null) {
			pageNo=1;
		}
		if (pageSize==null) {
			pageSize=10;
		}
		Integer authorUserId=null;
		if(StringUtils.isNotBlank(author)){
			CmsUser authorUser=userMng.findByUsername(author);
			if(authorUser!=null){
				authorUserId=authorUser.getId();
			}else{
				authorUserId=0;
			}
		}
		Pagination page = contentChargeMng.getPage(contentTitle,
				authorUserId,buyTimeBegin, buyTimeEnd,orderBy,pageNo,pageSize);
		List<ContentCharge> list = (List<ContentCharge>) page.getList();
		int totalCount = page.getTotalCount();
		JSONArray jsonArray = new JSONArray();
		if (list!=null&&list.size()>0) {
			for (int i = 0; i < list.size(); i++) {
				jsonArray.put(i,list.get(i).convertToJson());
			}
		}
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		String body = jsonArray.toString()+",\"totalCount\":"+totalCount;
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/contentBuy/user_account_list")
	public void userAccountList(String username,
			Date drawTimeBegin,Date drawTimeEnd,Integer orderBy,Integer pageNo,Integer pageSize,
			HttpServletRequest request,HttpServletResponse response){
		if (orderBy==null) {
			orderBy = 1;
		}
		if (pageNo==null) {
			pageNo=1;
		}
		if (pageSize==null) {
			pageSize=10;
		}
		Pagination page = userAccountMng.getPage(username,
				drawTimeBegin,drawTimeEnd,orderBy,pageNo,pageSize);
		int totalCount = page.getTotalCount();
		List<CmsUserAccount> list = (List<CmsUserAccount>) page.getList();
		JSONArray jsonArray = new JSONArray();
		if (list!=null&&list.size()>0) {
			for (int i = 0; i < list.size(); i++) {
				jsonArray.put(i,list.get(i).convertToJson());
			}
		}
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		String body = jsonArray.toString()+",\"totalCount\":"+totalCount;
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/contentBuy/user_order_list")
	public void userBuyList(String orderNum,String buyusername,
			String authorusername,Short payMode,Integer pageNo,Integer pageSize,
			HttpServletRequest request,HttpServletResponse response){
		if (pageNo==null) {
			pageNo=1;
		}
		if (pageSize==null) {
			pageSize=10;
		}
		Integer buyUserId=null,authorUserId=null;
		if(StringUtils.isNotBlank(buyusername)){
			CmsUser u=userMng.findByUsername(buyusername);
			if(u!=null){
				buyUserId=u.getId();
			}
		}
		if(StringUtils.isNotBlank(authorusername)){
			CmsUser u=userMng.findByUsername(authorusername);
			if(u!=null){
				authorUserId=u.getId();
			}
		}
		if(payMode==null){
			payMode=0;
		}
		Pagination page = contentBuyMng.getPage(orderNum, buyUserId, authorUserId, 
				payMode,pageNo,pageSize);
		int totalCount = page.getTotalCount();
		List<ContentBuy> list = (List<ContentBuy>) page.getList();
		JSONArray jsonArray = new JSONArray();
		if (list!=null&&list.size()>0) {
			for (int i = 0; i < list.size(); i++) {
				jsonArray.put(i,list.get(i).convertToJson());
			}
		}
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		String body = jsonArray.toString()+",\"totalCount\":"+totalCount;
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@RequestMapping("/contentBuy/commissionStatic")
	public void commissionStatic(HttpServletRequest request,HttpServletResponse response){
		CmsConfigContentCharge bean = configContentChargeMng.getDefault();
		String body = bean.convertToJson().toString();
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@Autowired
	private ContentChargeMng contentChargeMng;
	@Autowired
	private CmsUserAccountMng userAccountMng;
	@Autowired
	private ContentBuyMng contentBuyMng;
	@Autowired
	private CmsUserMng userMng;
	@Autowired
	private CmsConfigContentChargeMng configContentChargeMng;
}
