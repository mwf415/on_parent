package cn.onlov.on_cms_admin.plug;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;

import javax.persistence.EntityManager;

@SpringBootConfiguration
public class JpaSessionConfig {
    @Autowired
    EntityManager entityManager;

    @Bean
    public HibernateJpaSessionFactoryBean sessionFactory() {

        return new HibernateJpaSessionFactoryBean();
    }

    @Bean
    public Session entitySession() {
        Session session = this.entityManager.unwrap(Session.class);
        return session;
    }




}
