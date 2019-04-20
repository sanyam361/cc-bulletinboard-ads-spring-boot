package com.sap.bulletinboard.ads.multitenancy;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.bulletinboard.ads.multitenancy.beans.AvailableTenants;
import com.sap.bulletinboard.ads.multitenancy.beans.TenantContext;
import com.sap.bulletinboard.ads.multitenancy.beans.TenantSchemaGenerator;

@Configuration
public class MultiTenantConfiguration {

    @Bean
    public AvailableTenants availableTenants(@Value("${app.tenants}") String[] tenantIds) {
        List<String> tenantIdsList = Arrays.asList(tenantIds);
        return new AvailableTenants(tenantIdsList);
    }

    @Bean
    public TenantContext tentantContext(AvailableTenants availableTenants) {
        return new TenantContext(availableTenants);
    }

    @Bean(name = "tenantSchemaGenerator")
    public TenantSchemaGenerator tenantSchemaGenerator(DataSource dataSource, AvailableTenants tenants) {
        List<String> schemaNames = tenants.getAllSchemaNames();
        return new TenantSchemaGenerator(dataSource, schemaNames);
    }

}
