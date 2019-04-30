package cn.onlov.on_cms_admin.cms.api.member;

import cn.onlov.on_cms_common.cms.api.ApiResponse;
import cn.onlov.on_cms_common.cms.api.ApiValidate;
import cn.onlov.on_cms_common.cms.api.Constants;
import cn.onlov.on_cms_common.cms.api.ResponseCode;
import cn.onlov.on_cms_common.cms.entity.main.ContentBuy;
import cn.onlov.on_cms_common.cms.entity.main.ContentCharge;
import cn.onlov.on_cms_common.cms.manager.main.ContentBuyMng;
import cn.onlov.on_cms_common.cms.manager.main.ContentChargeMng;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import cn.onlov.on_cms_common.core.entity.CmsUser;
import cn.onlov.on_cms_common.core.web.WebErrors;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class ContentOrderApiAct {
	private static final int OPERATOR_BUY=1;
	private static final int OPERATOR_ORDER=2;
	private static final int OPERATOR_CHARGELIST=3;
	
	/**
	 * 我消费的记录和我的内容被打赏记录
	 * @param orderNum 订单号 非必选
	 * @param orderType 类型  1我消费的记录 2我的内容被打赏记录 默认1
	 * @param appId      appid  必选
	 * @param sessionKey 用户会话  必选
	 * @param first 非必选 默认0
	 * @param count 非必选 默认10
	 */
	@RequestMapping(value = "/order/myorders")
	public void myOrderList(String orderNum,Integer orderType,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					 {
		getMyInfoList(orderType, orderNum, 
				first, count, request, response);
	}
	
	/**
	 * 我的内容收费统计
	 * @param orderNum 订单号 非必选
	 * @param appId      appid  必选
	 * @param sessionKey 用户会话  必选
	 * @param first 非必选 默认0
	 * @param count 非必选 默认10
	 */
	@RequestMapping(value = "/order/chargelist")
	public void chargeList(String orderNum,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) 
					 {
		getMyInfoList(OPERATOR_CHARGELIST, orderNum,
				first, count, request, response);
	}
	
	private void getMyInfoList(Integer operate,String orderNum,
			Integer first,Integer count,
			HttpServletRequest request,HttpServletResponse response) {
		String body="\"\"";
		String message=Constants.API_STATUS_FAIL;
		String code=ResponseCode.API_CODE_CALL_SUCCESS;
		if(operate==null){
			operate=OPERATOR_BUY;
		}
		if(first==null){
			first=0;
		}
		if(count==null){
			count=10;
		}
		WebErrors errors=WebErrors.create(request);
		CmsUser user = CmsUtils.getUser(request);
		//验证公共非空参数
		errors=ApiValidate.validateRequiredParams(request,errors);
		if(!errors.hasErrors()){
			JSONArray jsonArray=new JSONArray();
			List<ContentBuy>list = null;
			List<ContentCharge>chargeList=null;
			if(OPERATOR_BUY==operate){
				list=contentBuyMng.getList(orderNum,
						user.getId(), null, null, first, count);
			}else if(OPERATOR_ORDER==operate){
				list=contentBuyMng.getList(orderNum,
						null, user.getId(), null, first, count);
			}else if(OPERATOR_CHARGELIST==operate){
				chargeList=contentChargeMng.getList(null, user.getId(),
						null, null, 1, first, count);
			}
			if(list!=null&&list.size()>0){
				for(int i=0;i<list.size();i++){
					jsonArray.put(i, list.get(i).convertToJson());
				}
			}
			if(chargeList!=null&&chargeList.size()>0){
				for(int i=0;i<chargeList.size();i++){
					jsonArray.put(i, chargeList.get(i).convertToJson());
				}
			}
			body=jsonArray.toString();
			message=Constants.API_MESSAGE_SUCCESS;
			code = ResponseCode.API_CODE_CALL_SUCCESS;
		}
		ApiResponse apiResponse=new ApiResponse(request, body, message,code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@Autowired
	private ContentBuyMng contentBuyMng;
	@Autowired
	private ContentChargeMng contentChargeMng;
}
