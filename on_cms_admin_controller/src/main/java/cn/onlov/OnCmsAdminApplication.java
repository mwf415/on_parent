package cn.onlov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ServletComponentScan(basePackages = "cn.onlov.cms")
@EnableWebMvc
public class OnCmsAdminApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OnCmsAdminApplication.class, args);
    }

//
//    @Bean
//    public SessionFactory sessionFactory(HibernateEntityManagerFactory hemf) {
//        return hemf.getSessionFactory();
//    }




}
