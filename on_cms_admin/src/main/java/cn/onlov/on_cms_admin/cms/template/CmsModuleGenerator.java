package cn.onlov.on_cms_admin.cms.template;


public class CmsModuleGenerator {
	private static String packName = "cn.onlov.on_cms_common.cms.template";
	private static String fileName = "jeecms.properties";

	public static void main(String[] args) {
		new ModuleGenerator(packName, fileName).generate();
	}
}
