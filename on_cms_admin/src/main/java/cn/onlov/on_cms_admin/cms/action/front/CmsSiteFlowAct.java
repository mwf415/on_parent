package cn.onlov.on_cms_admin.cms.action.front;


import cn.onlov.cms.common.cms.service.CmsSiteFlowCache;
import cn.onlov.cms.common.common.web.ResponseUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CmsSiteFlowAct {
	@RequestMapping("/flow_statistic.jspx")
	public void flowStatistic(HttpServletRequest request,
			HttpServletResponse response, String page) throws JSONException {
		Long[] counts = null;
		String referer=request.getParameter("referer");
		if (!StringUtils.isBlank(page)) {
			counts=cmsSiteFlowCache.flow(request, page, referer);
		} 
		String json;
		if (counts != null) {
			json = new JSONArray(counts).toString();
			ResponseUtils.renderJson(response, json);
		} else {
			ResponseUtils.renderJson(response, "[]");
		}
	}
	
	@Autowired
	private CmsSiteFlowCache cmsSiteFlowCache;
}
