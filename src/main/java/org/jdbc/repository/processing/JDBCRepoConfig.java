package org.jdbc.repository.processing;


import org.jdbc.repository.JDBCRepository;
import org.jdbc.repository.exception.JDBCRepositoryException;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.jdbc.repository.classscan.ClassScanner.scanClasses;

@Configuration
@ComponentScan
public class JDBCRepoConfig {

    @Value("${jdbc.repo.base.package}")
    private String basePackage;


    @Bean
    public ProxyFactoryBean makeUserRepoProxies(JDBCRepoInterceptor jdbcRepoInterceptor, ApplicationContext applicationContext) {
        basePackage = getBasePackage(applicationContext, List.of("makeUserRepoProxies"));
        List<Class<?>> classes = scanClasses(basePackage, type -> JDBCRepository.class.isAssignableFrom(type) && type != JDBCRepository.class);

        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setInterfaces(classes.get(0));
        proxyFactoryBean.setInterceptorNames(jdbcRepoInterceptor.getClass().getSimpleName());
        return proxyFactoryBean;
    }


    private String getBasePackage(ApplicationContext context, List<String> beansToNotLoad) {
        if (basePackage != null) {
            return basePackage;
        }

        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            boolean springOrOtherDependency = beanName.contains("spring") || beanName.contains("mock");
            if ( !springOrOtherDependency
                    && context.containsBeanDefinition(beanName)
                    && !beansToNotLoad.contains(beanName)) {
                Object bean = context.getBean(beanName);
                Class<?> beanType = bean.getClass();
                if (beanType.isAnnotationPresent(SpringBootApplication.class)) {
                    return bean.getClass().getPackageName();
                }
            }
        }

        for (String beanName : beanNames) {
            if (context.containsBeanDefinition(beanName) && !beansToNotLoad.contains(beanName)) {
                Object bean = context.getBean(beanName);
                Class<?> beanType = bean.getClass();
                if (beanType.isAnnotationPresent(SpringBootApplication.class)) {
                    return bean.getClass().getPackageName();
                }
            }
        }
        throw new JDBCRepositoryException("Could not find base package for building JDBCRepositories, " +
                "make sure you have a class annotated with @SpringBootApplication " +
                "at your project root, or specify your base package with the property: 'jdbc.repo.base.package'.");
    }



}
