package cn.onlov.cms.admin.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;

@Configuration
public class FilterConfiguration {

//
//    @Bean
//    public FilterRegistrationBean delegatingFilterProxy(){
//        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
//        filterRegistrationBean.addUrlPatterns("/*");
//        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
//        proxy.setTargetFilterLifecycle(true);
//        proxy.setTargetBeanName("shiroFilter");
//        filterRegistrationBean.setFilter(proxy);
//        return filterRegistrationBean;
//    }




//    @Bean("shiroFilter")
//    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
//        System.out.println("ShiroConfiguration.shirFilter()");
//        /**
//         * 准备数据
//         */
//
//        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
//
//        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager() ;
//
//        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
//        Map<String, Filter> filters = new HashMap();
//
//        EhCacheManager ehCacheManager = new EhCacheManager();
//        SimpleCookie rememberMeCookie = new SimpleCookie();
//        rememberMeCookie.setHttpOnly(true);
//        rememberMeCookie.setMaxAge(31536000);
//
//        CmsAuthorizingRealm realm = new CmsAuthorizingRealm();
//        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
//        credentialsMatcher.setHashAlgorithmName("MD5");
//        credentialsMatcher.setStoredCredentialsHexEncoded(true);
//        credentialsMatcher.setHashIterations(1);
//
//        CmsAuthenticationFilter authcFilter = new CmsAuthenticationFilter();
//        authcFilter.setAdminIndex("/jeeadmin/jeecms/index.do");
//
//        authcFilter.setAdminPrefix("/jeeadmin/jeecms/");
//        authcFilter.setAdminLogin("/jeeadmin/jeecms/login.do");
//
//        CmsUserFilter userFilter = new CmsUserFilter();
//        userFilter.setAdminPrefix("/jeeadmin/jeecms/");
//        userFilter.setAdminLogin("/jeeadmin/jeecms/login.do");
//
//        CmsLogoutFilter logoutFilter = new CmsLogoutFilter();
//        logoutFilter.setAdminPrefix("/jeeadmin/jeecms/");
//        logoutFilter.setAdminLogin("/jeeadmin/jeecms/login.do");
//
//
//
//
//
//
//        /**
//         * 使用数据
//         */
//        realm.setCredentialsMatcher(credentialsMatcher);
//        securityManager.setRealm(realm);
//
//        rememberMeManager.setCookie(rememberMeCookie);
//
//        securityManager.setCacheManager(ehCacheManager);
//        securityManager.setRememberMeManager(rememberMeManager);
//
//
//        filters.put("authc",authcFilter);
//        filters.put("user",userFilter);
//        filters.put("logout",logoutFilter);
//        factoryBean.setSecurityManager(securityManager);
//        factoryBean.setLoginUrl("/login.jspx");
//        factoryBean.setSuccessUrl("/");
//        factoryBean.setFilters(filters);
//        factoryBean.setFilterChainDefinitions("");
//
//
//
//        return factoryBean;
//    }


    @Bean
    public FilterRegistrationBean osivFilter() {
        FilterRegistrationBean<OpenSessionInViewFilter> registration = new FilterRegistrationBean<>();
        OpenSessionInViewFilter openSessionInViewFilter = new OpenSessionInViewFilter();

        //注入过滤器
        registration.setFilter(openSessionInViewFilter);
        //过滤规则
        registration.addUrlPatterns("*.do","*.jhtml","*.htm","*.jspx","*.jsp","/api/*","/");
        //过滤器名称
        registration.setName("osivFilter");
        return registration;
    }







}
