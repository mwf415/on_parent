package cn.onlov.on_cms_admin.core.action.front;

import cn.onlov.cms.common.common.web.RequestUtils;
import cn.onlov.cms.common.common.web.session.SessionProvider;
import cn.onlov.cms.common.core.entity.Authentication;
import cn.onlov.cms.common.core.manager.AuthenticationMng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.onlov.cms.common.core.manager.AuthenticationMng.AUTH_KEY;

/**
 * 登录处理Action
 * 
 * 登录成功后的处理类
 */
@Controller
public class ProcessAct {
	private static Logger log = LoggerFactory.getLogger(ProcessAct.class);

	@RequestMapping(value = "/process.jspx", method = RequestMethod.GET)
	public String process(HttpServletRequest request,
			HttpServletResponse response) {
		String returnUrl = RequestUtils.getQueryParam(request,
				FrontLoginAct.RETURN_URL);
		String authId = RequestUtils.getQueryParam(request, AUTH_KEY);
		Authentication auth = authMng.retrieve(authId);
		if (auth != null) {
			authMng.storeAuthIdToSession(session, request, response, auth
					.getId());
		} else {
			log.warn("Authentication id not found: {}", authId);
		}
		return "redirect:" + returnUrl;
	}

	@Autowired
	private AuthenticationMng authMng;
	@Autowired
	private SessionProvider session;
}
