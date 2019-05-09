package cn.onlov.on_cms_admin.cms.action.front;

import cn.onlov.cms.common.cms.entity.assist.CmsSearchWords;
import cn.onlov.cms.common.cms.manager.assist.CmsSearchWordsMng;
import cn.onlov.cms.common.cms.service.SearchWordsCache;
import cn.onlov.cms.common.cms.web.Token;
import cn.onlov.cms.common.common.util.StrUtils;
import cn.onlov.cms.common.common.web.RequestUtils;
import cn.onlov.cms.common.common.web.ResponseUtils;
import cn.onlov.cms.common.common.web.session.SessionProvider;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.web.WebErrors;
import cn.onlov.cms.common.core.web.util.CmsUtils;
import cn.onlov.cms.common.core.web.util.FrontUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.onlov.cms.common.cms.Constants.TPLDIR_SPECIAL;

@Controller
public class SearchAct {
	public static final String SEARCH_INPUT = "tpl.searchInput";
	public static final String SEARCH_RESULT = "tpl.searchResult";
	public static final String SEARCH_ERROR = "tpl.searchError";
	public static final String SEARCH_JOB = "tpl.searchJob";
	
	@Token(remove=true)
	@RequestMapping(value = "/search*.jspx", method = RequestMethod.GET)
	public String index(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		// 将request中所有参数保存至model中。
		model.putAll(RequestUtils.getQueryParams(request));
		FrontUtils.frontData(request, model, site);
		FrontUtils.frontPageData(request, model);
		
		try {
			String q = RequestUtils.getQueryParam(request, "q");
			String channelId = RequestUtils.getQueryParam(request, "channelId");
			if (StringUtils.isBlank(q) && StringUtils.isBlank(channelId)) {
				return FrontUtils.getTplPath(request, site.getSolutionPath(),
						TPLDIR_SPECIAL, SEARCH_INPUT);
			} else {
				WebErrors errors=WebErrors.create(request);
				if(StringUtils.isNotBlank(channelId)&&!StrUtils.isGreaterZeroNumeric(channelId)){
					errors.addErrorCode("error.channelId.notNum");
					return FrontUtils.showError(request, response, model, errors);
				}else{
					if(StringUtils.isNotBlank(q) && q.length()>30){
						q = q.substring(0, 30);
						model.addAttribute("maxLength", true);
					}
					String parseQ=parseKeywords(q);
					model.addAttribute("input",q);
					model.addAttribute("q",parseQ);
					searchWordsCache.cacheWord(q);
					return FrontUtils.getTplPath(request, site.getSolutionPath(),
							TPLDIR_SPECIAL, SEARCH_RESULT);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			model.addAttribute("error", true);
			return FrontUtils.getTplPath(request, site.getSolutionPath(),
					TPLDIR_SPECIAL, SEARCH_INPUT);
		}
		
	}
	
	@Token(remove=true)
	@RequestMapping(value = "/searchJob*.jspx", method = RequestMethod.GET)
	public String searchJob(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		String q = RequestUtils.getQueryParam(request, "q");
		String category = RequestUtils.getQueryParam(request, "category");
		String workplace = RequestUtils.getQueryParam(request, "workplace");
		String parseQ="";
		model.putAll(RequestUtils.getQueryParams(request));
		FrontUtils.frontData(request, model, site);
		FrontUtils.frontPageData(request, model);
		if (StringUtils.isBlank(q)) {
			model.remove("q");
		}else{
			//处理lucene查询字符串中的关键字
			parseQ=parseKeywords(q);
			parseQ=StrUtils.xssEncode(parseQ);
			if(!q.equals(parseQ)){
				return "redirect:searchJob.jspx";
			}
			model.addAttribute("q",parseQ);
		}
		model.addAttribute("input",parseQ);
		model.addAttribute("queryCategory",category);
		model.addAttribute("queryWorkplace",workplace);
		return FrontUtils.getTplPath(request, site.getSolutionPath(),
				TPLDIR_SPECIAL, SEARCH_JOB);
	}
	
	@RequestMapping(value = "/createToken.jspx")
	public void createToken(HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		JSONObject json=new JSONObject();
		String token=UUID.randomUUID().toString();
		try {
			json.put("token", token);
		} catch (JSONException e) {
		}
        sessionProvider.setAttribute(request, response, "token", token);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequestMapping("/search/v_ajax_list.jspx")
	public void ajaxList(HttpServletRequest request,HttpServletResponse response, ModelMap model) throws JSONException {
		JSONObject object = new JSONObject();
		Map<String,String>wordsMap=new LinkedHashMap<String, String>();
		String word=RequestUtils.getQueryParam(request, "term");
		if(StringUtils.isNotBlank(word)){
			List<CmsSearchWords>words=manager.getList(CmsUtils.getSiteId(request),
					word,null,CmsSearchWords.HIT_DESC,0,20,true);
			for(CmsSearchWords w:words){
				wordsMap.put(w.getName(), w.getName());
			}
		}
		object.put("words", wordsMap);
		ResponseUtils.renderJson(response, object.get("words").toString());
	}
	
	@RequestMapping(value = "/searchCustom*.jspx")
	public String searchCustom(String tpl, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		if(StringUtils.isNotBlank(tpl)){
			// 将request中所有参数保存至model中。
			model.putAll(RequestUtils.getQueryParams(request));
			FrontUtils.frontData(request, model, site);
			FrontUtils.frontPageData(request, model);
			return FrontUtils.getTplPath(site.getSolutionPath(), TPLDIR_SPECIAL,
					tpl);
		}else{
			return FrontUtils.pageNotFound(request, response, model);
		}
	}
	
	public static String parseKeywords(String q){
		char c='\\';
		int cIndex=q.indexOf(c);
		if(cIndex!=-1&&cIndex==0){
			q=q.substring(1);
		}
		if(cIndex!=-1&&cIndex==q.length()-1){
			q=q.substring(0,q.length()-1);
		}
		try {
			String regular = "[\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:\\\\]";
			Pattern p = Pattern.compile(regular);
			Matcher m = p.matcher(q);
			String src = null;
			while (m.find()) {
				src = m.group();
				q = q.replaceAll("\\" + src, ("\\\\" + src));
			}
			q = q.replaceAll("AND", "and").replaceAll("OR", "or").replace("NOT", "not").replace("[", "［").replace("]", "］");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  q;
	}

	@Autowired
	private CmsSearchWordsMng manager;
	@Autowired
	private SearchWordsCache searchWordsCache;
	@Autowired
	private SessionProvider sessionProvider;
}
