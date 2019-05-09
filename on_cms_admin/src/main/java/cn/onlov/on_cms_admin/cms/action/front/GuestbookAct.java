package cn.onlov.on_cms_admin.cms.action.front;

import cn.onlov.cms.common.cms.entity.assist.CmsGuestbook;
import cn.onlov.cms.common.cms.entity.assist.CmsGuestbookCtg;
import cn.onlov.cms.common.cms.manager.assist.CmsGuestbookCtgMng;
import cn.onlov.cms.common.cms.manager.assist.CmsGuestbookMng;
import cn.onlov.cms.common.cms.manager.assist.CmsSensitivityMng;
import cn.onlov.cms.common.cms.web.Token;
import cn.onlov.cms.common.common.web.RequestUtils;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.common.web.session.SessionProvider;
import cn.onlov.cms.common.core.entity.CmsConfig;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.entity.CmsUser;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import cn.onlov.cms.common.core.web.util.FrontUtils;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static cn.onlov.cms.common.cms.Constants.TPLDIR_SPECIAL;

@Controller
public class GuestbookAct {
	private static final Logger log = LoggerFactory
			.getLogger(GuestbookAct.class);

	public static final String GUESTBOOK_INDEX = "tpl.guestbookIndex";
	public static final String GUESTBOOK_CTG = "tpl.guestbookCtg";
	public static final String GUESTBOOK_DETAIL = "tpl.guestbookDetail";

	/**
	 * 留言板首页或类别页
	 * 
	 * @param ctgId
	 *            留言类别
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@Token(save=true)
	@RequestMapping(value = "/guestbook*.jspx", method = RequestMethod.GET)
	public String index(Integer ctgId, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		FrontUtils.frontData(request, model, site);
		FrontUtils.frontPageData(request, model);
		CmsGuestbookCtg ctg = null;
		if (ctgId != null) {
			ctg = cmsGuestbookCtgMng.findById(ctgId);
		}
		if (ctg == null) {
			// 留言板首页
			return FrontUtils.getTplPath(request, site.getSolutionPath(),
					TPLDIR_SPECIAL, GUESTBOOK_INDEX);
		} else {
			// 留言板类别页
			model.addAttribute("ctg", ctg);
			return FrontUtils.getTplPath(request, site.getSolutionPath(),
					TPLDIR_SPECIAL, GUESTBOOK_CTG);
		}
	}

	@Token(save=true)
	@RequestMapping(value = "/guestbook/{id}.jspx", method = RequestMethod.GET)
	public String detail(@PathVariable Integer id, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsGuestbook guestbook = null;
		if (id != null) {
			guestbook = cmsGuestbookMng.findById(id);
		}
		model.addAttribute("guestbook", guestbook);
		FrontUtils.frontData(request, model, site);
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				TPLDIR_SPECIAL, GUESTBOOK_DETAIL);

	}

	/**
	 * 提交留言。ajax提交。
	 * 
	 * @param contentId
	 * @param pageNo
	 * @param request
	 * @param response
	 * @param model
	 * @throws JSONException
	 * @throws IOException 
	 */
	@Token(remove=true)
	@RequestMapping(value = "/guestbook.jspx", method = RequestMethod.POST)
	public void submit(Integer siteId, Integer ctgId, String title,
			String content, String email, String phone, String qq,
			String captcha,String sessionId, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws JSONException, IOException {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser member = CmsUtils.getUser(request);
		CmsConfig config=site.getConfig();
		if (siteId == null) {
			siteId = site.getId();
		}
		JSONObject json = new JSONObject();
		try {
			if (!imageCaptchaService.validateResponseForID(session
					.getSessionId(request, response), captcha)) {
				json.put("success", false);
				json.put("status", 1);
				ResponseUtils.renderJson(response, json.toString());
				return;
			}
		} catch (CaptchaServiceException e) {
			json.put("success", false);
			json.put("status", 1);
			ResponseUtils.renderJson(response, json.toString());
			log.warn("", e);
			return;
		}
		//留言尚未开启
		if(!config.getGuestbookOpen()){
			json.put("success", false);
			json.put("status", 2);
			ResponseUtils.renderJson(response, json.toString());
			return;
		}
		//需要用户登陆
		if (member == null &&config.getGuestbookNeedLogin()) {
			json.put("success", false);
			json.put("status", 3);
			ResponseUtils.renderJson(response, json.toString());
			return;
		}
		if(member!=null){
			Integer dayLimit=config.getGuestbookDayLimit();
			//0 不限制留言数   大于限制数则不允许发
			if(dayLimit!=0&&dayLimit<=member.getUserExt().getTodayGuestbookTotal()){
				json.put("success", false);
				json.put("status", 4);
				ResponseUtils.renderJson(response, json.toString());
				return;
			}
		}
		boolean haveSensitive=sensitivityMng.haveSensitivity(content,title);
		if(haveSensitive){
			json.put("success", false);
			json.put("status", 10);
			ResponseUtils.renderJson(response, json.toString());
			return;
		}
		String ip = RequestUtils.getIpAddr(request);
		cmsGuestbookMng.save(member, siteId, ctgId, ip, title, content, email,
				phone, qq);
		json.put("success", true);
		json.put("status", 0);
		ResponseUtils.renderJson(response, json.toString());
	}

	@Autowired
	private CmsGuestbookCtgMng cmsGuestbookCtgMng;
	@Autowired
	private CmsGuestbookMng cmsGuestbookMng;
	@Autowired
	private SessionProvider session;
	@Autowired
	private ImageCaptchaService imageCaptchaService;
	@Autowired
	private CmsSensitivityMng sensitivityMng;

}
