package cn.onlov.on_cms_admin.cms.api.admin.main;

import cn.onlov.on_cms_common.cms.manager.assist.CmsWebserviceMng;
import cn.onlov.on_cms_common.cms.manager.main.ChannelMng;
import cn.onlov.on_cms_common.core.manager.*;
import cn.onlov.on_cms_common.core.security.CmsAuthorizingRealm;
import org.springframework.beans.factory.annotation.Autowired;

public class CmsAdminAbstractApi {
	@Autowired
	protected CmsSiteMng cmsSiteMng;
	@Autowired
	protected ChannelMng channelMng;
	@Autowired
	protected CmsRoleMng cmsRoleMng;
	@Autowired
	protected CmsGroupMng cmsGroupMng;
	@Autowired
	protected CmsLogMng cmsLogMng;
	@Autowired
	protected CmsUserMng manager;
	@Autowired
	protected CmsWebserviceMng cmsWebserviceMng;
	@Autowired
	protected CmsAuthorizingRealm authorizingRealm;
}
