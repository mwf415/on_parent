package cn.onlov.on_cms_admin.cms.action.front;

import cn.onlov.cms.common.cms.entity.main.Channel;
import cn.onlov.cms.common.cms.entity.main.CmsTopic;
import cn.onlov.cms.common.cms.entity.main.Content;
import cn.onlov.cms.common.cms.manager.assist.CmsKeywordMng;
import cn.onlov.cms.common.cms.manager.main.ChannelMng;
import cn.onlov.cms.common.cms.manager.main.CmsTopicMng;
import cn.onlov.cms.common.cms.manager.main.ContentMng;
import cn.onlov.cms.common.common.page.Paginable;
import cn.onlov.cms.common.common.page.SimplePage;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.web.WebErrors;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import cn.onlov.cms.common.core.web.util.FrontUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.onlov.cms.common.cms.Constants.TPLDIR_VISUAL;

/**
 * @author Tom
 */

/**
 * 调用ajax生成的标签页面
 */
@Controller
public class VisualAct {
	/**
	 * 
	 * @param tempId
	 *            最大支持20
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/visual/getPage{tempId}.jspx", method = RequestMethod.GET)
	public String loadVisual(@PathVariable Integer tempId, Integer channelId,
			Integer contentId, Integer topicId, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		FrontUtils.frontData(request, model, site);
		FrontUtils.frontPageData(request, model);
		if (tempId < 0 || tempId > 20) {
			WebErrors errors = WebErrors.create(request);
			errors.addErrorCode("error.tempIdOutOfRange");
			return FrontUtils.showError(request, response, model, errors);
		}
		if(channelId!=null){
			Channel channel=channelMng.findById(channelId);
			model.addAttribute("channel",channel );
		}
		if(contentId!=null){
			Content content=contentMng.findById(contentId);
			String txt = content.getTxtByNo(1);
			// 内容加上关键字
			txt = cmsKeywordMng.attachKeyword(site.getId(), txt);
			Paginable pagination = new SimplePage(1, 1, content.getPageCount());
			model.addAttribute("pagination", pagination);
			FrontUtils.frontPageData(request, model);
			model.addAttribute("content", content);
			model.addAttribute("channel", content.getChannel());
			model.addAttribute("title", content.getTitleByNo(1));
			model.addAttribute("txt", txt);
			model.addAttribute("pic", content.getPictureByNo(1));
		}
		if(topicId!=null){
			CmsTopic topic =topicMng.findById(topicId);
			model.addAttribute("topic",topic);
		}
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				TPLDIR_VISUAL, "tpl.directive" + tempId);
	}
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private ContentMng contentMng;
	@Autowired
	private CmsTopicMng topicMng;
	@Autowired
	private CmsKeywordMng cmsKeywordMng;
}
