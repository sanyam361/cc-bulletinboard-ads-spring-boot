package com.sap.bulletinboard.ads.multitenancy;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.sap.bulletinboard.ads.BulletinboardAdsApplication;
import com.sap.bulletinboard.ads.multitenancy.beans.CurrentTenantIdentifierResolverImpl;
import com.sap.bulletinboard.ads.multitenancy.beans.MultiTenantConnectionProviderImpl;
import com.sap.bulletinboard.ads.multitenancy.beans.TenantContext;

@Configuration
public class HibernateConfiguration {

    @Bean
    @DependsOn("multiTenantSpringLiquibase")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
            TenantContext tenantContext, JpaProperties jpaProperties, JpaVendorAdapter adapter) {

        MultiTenantConnectionProvider multiTenantConnectionProvider = new MultiTenantConnectionProviderImpl(dataSource);
        CurrentTenantIdentifierResolver currentTenantIdentifierResolver = new CurrentTenantIdentifierResolverImpl(
                tenantContext);

        // These contain the default settings provided by Spring Boot and the settings
        // specified in the application.properties, translated to be used with
        // hibernate.
        // Starting from Spring Boot 2.1.0, the way this has to be done was changed. See
        // https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.1-Release-Notes
        Map<String, Object> hibernateProperties = jpaProperties.getHibernateProperties(new HibernateSettings());
        Map<String, ?> adapterProperties = adapter.getJpaPropertyMap();

        Map<String, Object> properties = new HashMap<>();
        properties.putAll(adapterProperties);
        properties.putAll(hibernateProperties);
        properties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
        properties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(BulletinboardAdsApplication.class.getPackage().getName());
        emf.setJpaVendorAdapter(adapter);
        emf.setJpaPropertyMap(properties);
        return emf;
    }

}