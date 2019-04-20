package com.sap.bulletinboard.ads.multitenancy;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.sap.bulletinboard.ads.multitenancy.beans.AvailableTenants;

import liquibase.integration.spring.MultiTenantSpringLiquibase;

@Configuration
public class LiquibaseMultiTenantConfiguration {

    @Bean(name = "multiTenantSpringLiquibase")
    @DependsOn("tenantSchemaGenerator")
    public MultiTenantSpringLiquibase multiTenantSpringLiquibase(DataSource dataSource, LiquibaseProperties properties,
            AvailableTenants availableTenants) {
        List<String> schemas = availableTenants.getAllSchemaNames();

        MultiTenantSpringLiquibase liquibase = createLiquibaseFromProperties(properties);
        liquibase.setDataSource(dataSource);
        liquibase.setSchemas(schemas);

        return liquibase;
    }

    private MultiTenantSpringLiquibase createLiquibaseFromProperties(LiquibaseProperties properties) {
        MultiTenantSpringLiquibase liquibase = new MultiTenantSpringLiquibase();
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setLabels(properties.getLabels());
        liquibase.setParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        return liquibase;
    }

}
