package cn.onlov.on_cms_admin.cms.action.front;

import cn.onlov.on_cms_common.cms.entity.main.Channel;
import cn.onlov.on_cms_common.cms.manager.main.ChannelMng;
import cn.onlov.on_cms_common.cms.service.ChannelCountCache;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ChannelCountAct {
	@RequestMapping(value = "/channel_view.jspx", method = RequestMethod.GET)
	public void contentView(Integer channelId, HttpServletRequest request,
			HttpServletResponse response) throws JSONException {
		if (channelId == null) {
			ResponseUtils.renderJson(response, "[]");
			return;
		}
		//栏目访问量计数
		Channel channel=channelMng.findById(channelId);
		int[] counts =channelCountCache.viewAndGet(channel.getId());
		String json;
		if (counts != null) {
			json = new JSONArray(counts).toString();
			ResponseUtils.renderJson(response, json);
		} else {
			ResponseUtils.renderJson(response, "[]");
		}
	}
	

	@Autowired
	private ChannelCountCache channelCountCache;
	@Autowired
	private ChannelMng channelMng;
}
