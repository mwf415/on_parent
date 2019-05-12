package cn.onlov.cms.admin.filter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration   //标注此文件为一个配置项，spring boot才会扫描到该配置。
@ImportResource(locations={"classpath:config/application-context.xml"})
public class HibernateBootConfiguration extends WebMvcConfigurerAdapter {

}