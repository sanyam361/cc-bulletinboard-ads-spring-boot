package com.sap.bulletinboard.ads.multitenancy.beans;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    private final TenantContext tenantContext;

    public CurrentTenantIdentifierResolverImpl(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        return tenantContext.getCurrentTenant().getSchemaName();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

}
