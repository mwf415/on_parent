package cn.onlov.on_cms_admin.cms.api.admin.main;


import cn.onlov.on_cms_common.cms.annotation.SignValidate;
import cn.onlov.on_cms_common.cms.api.ApiResponse;
import cn.onlov.on_cms_common.cms.api.ApiValidate;
import cn.onlov.on_cms_common.cms.api.Constants;
import cn.onlov.on_cms_common.cms.api.ResponseCode;
import cn.onlov.on_cms_common.common.web.ResponseUtils;
import cn.onlov.on_cms_common.common.web.springmvc.RealPathResolver;
import cn.onlov.on_cms_common.core.entity.CmsSite;
import cn.onlov.on_cms_common.core.manager.CmsSiteMng;
import cn.onlov.on_cms_common.core.web.WebErrors;
import cn.onlov.on_cms_common.core.web.util.CmsUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


@Controller
public class CmsFireWallConfigApiAct {
	public static final String FIREWALL_LOGIN = "firewall_login";
	public static final String FIREWALL_CONFIG_LASTMODIFIED = "firewall_config_lastmodified";
	String property_firewall_password = "firewall.password";
	String property_firewall_open = "firewall.open";
	String property_firewall_domain = "firewall.domain";
	String property_firewall_hour = "firewall.hour";
	String property_firewall_week = "firewall.week";
	String property_firewall_ips = "firewall.ips";
	
	@RequestMapping("/config/firewall_get")
	public void get(HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_SUCCESS;
		String code = ResponseCode.API_CODE_CALL_SUCCESS;
		InputStream in;
		try {
			in = new FileInputStream(realPathResolver.get(cn.onlov.on_cms_common.cms.Constants.FIREWALL_CONFIGPATH));
		Properties p = new Properties();
			p.load(in);
			String password = p.getProperty(property_firewall_password);
			String open = p.getProperty(property_firewall_open);
			String domain = p.getProperty(property_firewall_domain);
			String hour = p.getProperty(property_firewall_hour);
			String week = p.getProperty(property_firewall_week);
			String ips = p.getProperty(property_firewall_ips);
			String[] hours=StringUtils.split(hour, ",");
			Set<Integer>hourIds=new HashSet<Integer>();
			for(String h:hours){
				hourIds.add(Integer.decode(h));
			}
			String[] weeks=StringUtils.split(week, ",");
			Set<Integer>weekIds=new HashSet<Integer>();
			for(String w:weeks){
				weekIds.add(Integer.decode(w));
			}
			JSONObject json = new JSONObject();
			json.put("password", password);
			json.put("open", open);
			json.put("domain", domain);
			json.put("ips", ips);
			json.put("hours", hourIds);
			json.put("weeks", weekIds);
			body = json.toString();
		} catch (FileNotFoundException e) {
			message = Constants.API_MESSAGE_FILE_NOT_FOUNT;
			code = ResponseCode.API_CODE_FILE_NOT_FOUNT;
		} catch (Exception e) {
			message = "\"\"";
			code = ResponseCode.API_CODE_CALL_FAIL;
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	@SignValidate
	@RequestMapping("/config/firewall_update")
	public void update( String open,String valPassword,
			String password, String domain, String weeks,String hours, String ips,
			HttpServletRequest request,HttpServletResponse response){
		String body = "\"\"";
		String message = Constants.API_MESSAGE_PARAM_REQUIRED;
		String code = ResponseCode.API_CODE_PARAM_REQUIRED;
		WebErrors errors = WebErrors.create(request);
		errors = ApiValidate.validateRequiredParams(request, errors, open);
		if (!errors.hasErrors()) {
			boolean result = validatePassword(errors, valPassword, request, response);
			if (!result) {
				message = Constants.API_MESSAGE_PASSWORD_ERROR;
				code = ResponseCode.API_CODE_PASSWORD_ERROR;
			}else{
				InputStream in;
				try {
					in = new FileInputStream(realPathResolver.get(cn.onlov.on_cms_common.cms.Constants.FIREWALL_CONFIGPATH));
					Properties p = new Properties();
					p.load(in);
					if(StringUtils.isNotBlank(password)){
						p.setProperty(property_firewall_password, password);
					}
					p.setProperty(property_firewall_open, open);
					p.setProperty(property_firewall_domain, domain);
					CmsSite site= CmsUtils.getSite(request);
					configSiteDomainAlias(site, domain);
					p.setProperty(property_firewall_week,weeks);
					p.setProperty(property_firewall_hour,hours);
					p.setProperty(property_firewall_ips, ips);
					OutputStream out = new FileOutputStream(realPathResolver.get(cn.onlov.on_cms_common.cms.Constants.FIREWALL_CONFIGPATH));
					p.store(out, "update firewall config");
					message = Constants.API_MESSAGE_SUCCESS;
					code = ResponseCode.API_CODE_CALL_SUCCESS;
				} catch (FileNotFoundException e) {
					message = Constants.API_MESSAGE_FILE_NOT_FOUNT;
					code = ResponseCode.API_CODE_FILE_NOT_FOUNT;
				} catch (Exception e) {
					message = "\"\"";
					code = ResponseCode.API_CODE_CALL_FAIL;
				}
			}
		}
		ApiResponse apiResponse = new ApiResponse(request, body, message, code);
		ResponseUtils.renderApiJson(response, request, apiResponse);
	}
	
	private void configSiteDomainAlias(CmsSite site,String domain){
		if(StringUtils.isNotBlank(site.getDomainAlias())){
			if(!site.getDomainAlias().contains(domain)){
				site.setDomainAlias(site.getDomainAlias()+","+domain);
			}
		}else{
			site.setDomainAlias(domain);
		}
//		if(site.getUploadFtp()!=null){
//			if(site.getSyncPageFtp()!=null){
//				siteManager.update(site, site.getUploadFtp().getId(),site.getSyncPageFtp().getId());
//			}else{
//				siteManager.update(site, site.getUploadFtp().getId(),null);
//			}
//		}else{
//			if(site.getSyncPageFtp()!=null){
//				siteManager.update(site, null,site.getSyncPageFtp().getId());
//			}else{
//				siteManager.update(site, null,null);
//			}
//		}
		siteManager.update(site);
	}
	
	private boolean validatePassword(WebErrors errors,String password,HttpServletRequest request,HttpServletResponse response){
		boolean result = false;
		try {
			InputStream in = new FileInputStream(realPathResolver.get(cn.onlov.on_cms_common.cms.Constants.FIREWALL_CONFIGPATH));
			Properties p = new Properties();
			p.load(in);
			String pass = p.getProperty(property_firewall_password);
			if(pass.equals(password)){
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private CmsSiteMng siteManager;
}
