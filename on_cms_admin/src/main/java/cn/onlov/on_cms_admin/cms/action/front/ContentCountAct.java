package cn.onlov.on_cms_admin.cms.action.front;

import cn.onlov.on_cms_common.cms.entity.main.Channel;
import cn.onlov.on_cms_common.cms.entity.main.Content;
import cn.onlov.on_cms_common.cms.entity.main.ContentCount;
import cn.onlov.on_cms_common.cms.entity.main.ContentCount.ContentViewCount;
import cn.onlov.on_cms_common.cms.manager.main.ContentCountMng;
import cn.onlov.on_cms_common.cms.manager.main.ContentMng;
import cn.onlov.on_cms_common.cms.service.ChannelCountCache;
import cn.onlov.on_cms_common.cms.service.ContentCountCache;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ContentCountAct {
	@RequestMapping(value = "/content_view.jspx", method = RequestMethod.GET)
	public void contentView(Integer contentId, HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		if (contentId == null) {
			ResponseUtils.renderJson(response, "[]");
			return;
		}
		int[] counts = contentCountCache.viewAndGet(contentId);
		//栏目访问量计数
		if(contentId!=null){
			Content content=contentMng.findById(contentId);
			if(content!=null){
				Channel channel=content.getChannel();
				channelCountCache.viewAndGet(channel.getId());
				String json;
				if (counts != null) {
					json = new JSONArray(counts).toString();
					ResponseUtils.renderJson(response, json);
				} else {
					ResponseUtils.renderJson(response, "[]");
				}
			}
		}
	}
	
	@RequestMapping(value = "/content_view_get.jspx")
	public void getContentView(Integer contentIds[], String view, HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		if (contentIds == null) {
			ResponseUtils.renderJson(response, "[]");
			return;
		}
		ContentViewCount viewCountType;
		JSONObject json=new JSONObject();
		Map<Integer, Integer>contentViewsMap=new HashMap<Integer, Integer>();
		if (!StringUtils.isBlank(view)) {
			viewCountType = ContentViewCount.valueOf(view);
		} else {
			viewCountType = ContentViewCount.viewTotal;
		}
		for(Integer contentId:contentIds){
			Integer counts=getViewCount(contentId, viewCountType);
			if (counts != null) {
				contentViewsMap.put(contentId, counts);
			} else{
				contentViewsMap.put(contentId, 0);
			}
			json.put("contentViewsMap", contentViewsMap);
		}
		ResponseUtils.renderJson(response, json.toString());
	}

	@RequestMapping(value = "/content_up.jspx", method = RequestMethod.GET)
	public void contentUp(Integer contentId, HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		if (contentId == null) {
			ResponseUtils.renderJson(response, "false");
		} else {
			contentCountMng.contentUp(contentId);
			ResponseUtils.renderJson(response, "true");
		}
	}

	@RequestMapping(value = "/content_down.jspx", method = RequestMethod.GET)
	public void contentDown(Integer contentId, HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		if (contentId == null) {
			ResponseUtils.renderJson(response, "false");
		} else {
			contentCountMng.contentDown(contentId);
			ResponseUtils.renderJson(response, "true");
		}
	}
	
	private Integer getViewCount(Integer contentId,ContentViewCount viewCountType){
		Integer counts=0;
		ContentCount contentCount=contentCountMng.findById(contentId);
		if(viewCountType.equals(ContentViewCount.viewTotal)){
			counts= contentCount.getViews();
		}else if(viewCountType.equals(ContentViewCount.viewMonth)){
			counts=contentCount.getViewsMonth();
		}else if(viewCountType.equals(ContentViewCount.viewWeek)){
			counts=contentCount.getViewsWeek();
		}else if(viewCountType.equals(ContentViewCount.viewDay)){
			counts=contentCount.getViewsDay();
		}else{
			
		}
		return counts;
	}

	@Autowired
	private ContentCountCache contentCountCache;
	@Autowired
	private ChannelCountCache channelCountCache;
	@Autowired
	private ContentCountMng contentCountMng;
	@Autowired
	private ContentMng contentMng;
}