package cn.onlov.on_cms_admin.cms.action.member;

import cn.onlov.cms.common.cms.manager.main.ContentBuyMng;
import cn.onlov.cms.common.cms.manager.main.ContentChargeMng;
import cn.onlov.cms.common.common.page.Pagination;
import cn.onlov.cms.common.common.web.CookieUtils;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.entity.CmsUser;
import cn.onlov.cms.common.core.entity.MemberConfig;
import cn.onlov.cms.common.core.web.WebErrors;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import cn.onlov.cms.common.core.web.util.FrontUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.onlov.cms.common.cms.Constants.TPLDIR_MEMBER;
import static cn.onlov.cms.common.common.page.SimplePage.cpn;

/**
 * 用户账户相关
 * 包含笔者所写文章被用户购买记录
 * 自己的消费记录
 */
@Controller
public class UserContentBuyAct {
	
	public static final String MEMBER_BUY_LIST = "tpl.memberBuyList";
	public static final String MEMBER_ORDER_LIST = "tpl.memberOrderList";
	public static final String CONTENT_CHARGE_LIST = "tpl.memberContentChargeList";

	/**
	 * 自己消费记录
	 * @param pageNo
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/member/buy_list.jspx")
	public String buyList(String orderNum,Integer pageNo,HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		FrontUtils.frontData(request, model, site);
		MemberConfig mcfg = site.getConfig().getMemberConfig();
		// 没有开启会员功能
		if (!mcfg.isMemberOn()) {
			return FrontUtils.showMessage(request, model, "member.memberClose");
		}
		if (user == null) {
			return FrontUtils.showLogin(request, model, site);
		}
		Pagination pagination=contentBuyMng.getPage(orderNum,user.getId(),
				null,null,cpn(pageNo), CookieUtils.getPageSize(request));
		model.addAttribute("pagination",pagination);
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				TPLDIR_MEMBER, MEMBER_BUY_LIST);
	}
	
	/**
	 * 订单列表(被购买记录)
	 * @param pageNo
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/member/order_list.jspx")
	public String orderList(String orderNum,Integer pageNo,HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		FrontUtils.frontData(request, model, site);
		MemberConfig mcfg = site.getConfig().getMemberConfig();
		// 没有开启会员功能
		if (!mcfg.isMemberOn()) {
			return FrontUtils.showMessage(request, model, "member.memberClose");
		}
		if (user == null) {
			return FrontUtils.showLogin(request, model, site);
		}
		if(user.getUserAccount()==null){
			WebErrors errors=WebErrors.create(request);
			errors.addErrorCode("error.userAccount.notfound");
			return FrontUtils.showError(request, response, model, errors);
		}
		Pagination pagination=contentBuyMng.getPage(orderNum, null, user.getId(),
				null,cpn(pageNo), CookieUtils.getPageSize(request));
		model.addAttribute("pagination",pagination);
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				TPLDIR_MEMBER, MEMBER_ORDER_LIST);
	}
	
	/**
	 * 我的内容收益列表
	 * @param pageNo
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/member/charge_list.jspx")
	public String contentChargeList(Integer orderBy,
			Integer pageNo,HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		FrontUtils.frontData(request, model, site);
		MemberConfig mcfg = site.getConfig().getMemberConfig();
		// 没有开启会员功能
		if (!mcfg.isMemberOn()) {
			return FrontUtils.showMessage(request, model, "member.memberClose");
		}
		if (user == null) {
			return FrontUtils.showLogin(request, model, site);
		}
		if(user.getUserAccount()==null){
			WebErrors errors=WebErrors.create(request);
			errors.addErrorCode("error.userAccount.notfound");
			return FrontUtils.showError(request, response, model, errors);
		}
		if(orderBy==null){
			orderBy=1;
		}
		Pagination pagination=contentChargeMng.getPage(null,user.getId(),
				null,null,orderBy,cpn(pageNo), CookieUtils.getPageSize(request));
		model.addAttribute("pagination",pagination);
		model.addAttribute("orderBy", orderBy);
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				TPLDIR_MEMBER, CONTENT_CHARGE_LIST);
	}
	
	
	
	@Autowired
	private ContentBuyMng contentBuyMng;
	@Autowired
	private ContentChargeMng contentChargeMng;
}
