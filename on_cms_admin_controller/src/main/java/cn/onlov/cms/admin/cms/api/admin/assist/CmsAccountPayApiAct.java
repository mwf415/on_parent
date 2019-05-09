package cn.onlov.cms.admin.cms.api.admin.assist;

import cn.onlov.cms.common.cms.annotation.SignValidate;
import cn.onlov.cms.common.cms.api.ApiResponse;
import cn.onlov.cms.common.cms.api.ApiValidate;
import cn.onlov.cms.common.cms.api.Constants;
import cn.onlov.cms.common.cms.api.ResponseCode;
import cn.onlov.cms.common.cms.entity.assist.CmsAccountDraw;
import cn.onlov.cms.common.cms.entity.assist.CmsAccountPay;
import cn.onlov.cms.common.cms.entity.assist.CmsConfigContentCharge;
import cn.onlov.cms.common.cms.manager.assist.CmsAccountDrawMng;
import cn.onlov.cms.common.cms.manager.assist.CmsAccountPayMng;
import cn.onlov.cms.common.cms.manager.assist.CmsConfigContentChargeMng;
import cn.onlov.cms.common.common.page.Pagination;
import cn.onlov.cms.common.common.security.encoder.Md5PwdEncoder;
import cn.onlov.cms.common.common.util.Num62;
import cn.onlov.cms.common.common.util.PropertyUtils;
import cn.onlov.cms.common.common.util.StrUtils;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.common.web.springmvc.MessageResolver;
import cn.onlov.cms.common.common.web.springmvc.RealPathResolver;
import cn.onlov.cms.common.core.entity.CmsUser;
import cn.onlov.cms.common.core.manager.CmsUserMng;
import cn.onlov.cms.common.core.web.WebErrors;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;
import java.util.List;

@Controller
public class CmsAccountPayApiAct {
	private static final Logger log = LoggerFactory.getLogger(CmsAccountPayApiAct.class);
	public static final String PAY_LOGIN = "pay_login";
	public static final String WEIXIN_PAY_URL="weixin.transfer.url";
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/accountPay/list")
	public void list(String drawNum,String payUserName,String drawUserName,
			Date payTimeBegin,Date payTimeEnd,Integer pageNo,Integer pageSize,
			HttpServletRequest request,HttpServletResponse response){
		if (pageNo==null) {
			pageNo=1;
		}
		if (pageSize==null) {
			pageSize=10;
		}
		Integer payUserId=null;
		Integer drawUserId=null;
		if(StringUtils.isNotBlank(payUserName)){
			CmsUser payUser=userMng.findByUsername(payUserName);
			if(payUser!=null){
				payUserId=payUser.getId();
			}else{
				payUserId=0;
			}
		}
		if(StringUtils.isNotBlank(drawUserName)){
			CmsUser drawUser=userMng.findByUsername(drawUserName);
			if(drawUser!=null){
				drawUserId=drawUser.getId();
			}else{
				drawUserId=0;
			}
		}
		Pagination page = accountPayMng.getPage(drawNum, payUserId, drawUserId, payTimeBegin, payTimeEnd,
				pageNo, pageSize);
		int totalCount = page.getTotalCount();
		List<CmsAccountPay> list = (List<CmsAccountPay>) page.getList();
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
	
	@SignValidate
	@RequestMapping("/accountPay/payByWX")
	public void payByWX(Integer drawId, String password,HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		CmsUser user=CmsUtils.getUser(request);
		errors = ApiValidate.validateRequiredParams(request, errors, drawId,password);
		if (!errors.hasErrors()) {
			CmsConfigContentCharge config = configContentChargeMng.getDefault();
			if (pwdEncoder.encodePassword(password).equals(config.getPayTransferPassword())) {//判断密码是否正确
				CmsAccountDraw bean = accountDrawMng.findById(drawId);
				if (bean==null) {
					message = Constants.API_MESSAGE_OBJECT_NOT_FOUND;
					code = ResponseCode.API_CODE_NOT_FOUND;
				}else{
					if (StringUtils.isBlank(getWeixinPayUrl())) {
						setWeixinPayUrl(PropertyUtils.getPropertyValue(new File(realPathResolver.get(cn.onlov.cms.common.cms.Constants.JEECMS_CONFIG)), WEIXIN_PAY_URL));
					}
					String statu = accountPayMng.weixinTransferPay(getWeixinPayUrl(), drawId, bean.getDrawUser(), user, bean.getApplyAmount() 
							,System.currentTimeMillis()+RandomStringUtils.random(5,Num62.N10_CHARS), request, response, null);
					body = "{\"status\":\""+statu+"\"}";
					if (MessageResolver.getMessage(request,"transferPay.success").equals(statu)) {
						code = ResponseCode.API_CODE_CALL_SUCCESS;
						message = Constants.API_MESSAGE_SUCCESS;
					}else{
						message = Constants.API_MESSAGE_PAY_ERROR;
						code = ResponseCode.API_CODE_PAY_ERROR;
					}
				}
			}else{
				message = Constants.API_MESSAGE_PASSWORD_ERROR;
				code = ResponseCode.API_CODE_PASSWORD_ERROR;
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/accountPay/delete")
	public void delete(String ids,HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors, ids);
		if (!errors.hasErrors()) {
			Long[] idArr = StrUtils.getLongs(ids);
			errors = validateDelete(errors,idArr);
			if (errors.hasErrors()) {
				message = errors.getErrors().get(0);
				code = ResponseCode.API_CODE_NOT_FOUND;
			}else{
				CmsAccountPay[] beans = accountPayMng.deleteByIds(idArr);
				for (CmsAccountPay bean : beans) {
					log.info("delete CmsAccountPay id={}", bean.getId());
				}
				message = Constants.API_MESSAGE_SUCCESS;
				code = ResponseCode.API_CODE_CALL_SUCCESS;
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private WebErrors validateDelete(WebErrors errors, Long[] idArr) {
		if (idArr!=null) {
			for (int i = 0; i < idArr.length; i++) {
				CmsAccountPay pay = accountPayMng.findById(idArr[i]);
				if (pay==null) {
					errors.addErrorString(Constants.API_MESSAGE_OBJECT_NOT_FOUND);
					return errors;
				}
			}
		}
		return errors;
	}

	private String weixinPayUrl;

	public String getWeixinPayUrl() {
		return weixinPayUrl;
	}

	public void setWeixinPayUrl(String weixinPayUrl) {
		this.weixinPayUrl = weixinPayUrl;
	}
	
	@Autowired
	private CmsAccountDrawMng accountDrawMng;
	@Autowired
	private CmsAccountPayMng accountPayMng;
	@Autowired
	private Md5PwdEncoder pwdEncoder;
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private CmsConfigContentChargeMng configContentChargeMng;
	@Autowired
	private CmsUserMng userMng;
}
