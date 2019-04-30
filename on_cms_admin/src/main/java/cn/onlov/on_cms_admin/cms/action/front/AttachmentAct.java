package cn.onlov.on_cms_admin.cms.action.front;


import cn.onlov.on_cms_common.cms.entity.main.Content;
import cn.onlov.on_cms_common.cms.entity.main.ContentAttachment;
import cn.onlov.on_cms_common.cms.manager.main.ContentCountMng;
import cn.onlov.on_cms_common.cms.manager.main.ContentMng;
import cn.onlov.on_cms_common.common.security.encoder.PwdEncoder;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import cn.onlov.on_cms_common.common.web.springmvc.RealPathResolver;
import cn.onlov.on_cms_common.core.entity.CmsConfig;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class AttachmentAct {
	private static final Logger log = LoggerFactory.getLogger(AttachmentAct.class);

	@RequestMapping(value = "/attachment.jspx", method = RequestMethod.GET)
	public void attachment(Integer cid, Integer i, Long t, String k, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException {
		if (cid == null) {
			ResponseUtils.renderText(response, "downlaod error!");
		}
		CmsConfig config = CmsUtils.getSite(request).getConfig();
		String code = config.getDownloadCode();
		int h = config.getDownloadTime() * 60 * 60 * 1000;
		if (pwdEncoder.isPasswordValid(k, cid + ";" + i + ";" + t, code)) {
			long curr = System.currentTimeMillis();
			if (t + h > curr) {
				Content c = contentMng.findById(cid);
				if (c != null) {
					List<ContentAttachment> list = c.getAttachments();
					if (list.size() > i) {
						contentCountMng.downloadCount(c.getId());
						ContentAttachment ca = list.get(i);
						response.sendRedirect(ca.getPath());
						return;
					} else {
						log.info("download index is out of range: {}", i);
					}
				} else {
					log.info("Content not found: {}", cid);
				}
			} else {
				log.info("download time is expired!");
			}
		} else {
			log.info("download key error!");
		}
		ResponseUtils.renderText(response, "downlaod error!");
	}

	/**
	 * 获得下载key和time
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/attachment_url.jspx", method = RequestMethod.GET)
	public void url(Integer cid, Integer n, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		if (cid == null || n == null) {
			return;
		}
		CmsConfig config = CmsUtils.getSite(request).getConfig();
		String code = config.getDownloadCode();
		long t = System.currentTimeMillis();
		JSONArray arr = new JSONArray();
		String key;
		for (int i = 0; i < n; i++) {
			key = pwdEncoder.encodePassword(cid + ";" + i + ";" + t, code);
			arr.put("&t=" + t + "&k=" + key);
		}
		ResponseUtils.renderText(response, arr.toString());
	}
	
	@Autowired
	private ContentMng contentMng;
	@Autowired
	private ContentCountMng contentCountMng;
	@Autowired
	private PwdEncoder pwdEncoder;
	@Autowired
	private RealPathResolver realPathResolver;

}
