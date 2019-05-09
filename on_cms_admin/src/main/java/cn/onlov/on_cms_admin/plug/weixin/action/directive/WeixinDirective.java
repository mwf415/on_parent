package cn.onlov.on_cms_admin.plug.weixin.action.directive;

import cn.onlov.cms.common.common.web.freemarker.DefaultObjectWrapperBuilderFactory;
import cn.onlov.cms.common.common.web.freemarker.DirectiveUtils;
import cn.onlov.cms.common.core.entity.CmsSite;
import cn.onlov.cms.common.core.web.util.FrontUtils;
import cn.onlov.cms.common.plug.weixin.entity.Weixin;
import cn.onlov.cms.common.plug.weixin.manager.WeixinMng;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static cn.onlov.cms.common.common.web.freemarker.DirectiveUtils.OUT_BEAN;

public class WeixinDirective implements TemplateDirectiveModel {

	@SuppressWarnings("unchecked")
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		CmsSite site = FrontUtils.getSite(env);
		Weixin entity = manager.find(site.getId());

		Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(
				params);
		paramWrap.put(OUT_BEAN, DefaultObjectWrapperBuilderFactory.getDefaultObjectWrapper().wrap(entity));
		Map<String, TemplateModel> origMap = DirectiveUtils
				.addParamsToVariable(env, paramWrap);
		body.render(env.getOut());
		DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
	}
	
	@Autowired
	private WeixinMng manager;
}
