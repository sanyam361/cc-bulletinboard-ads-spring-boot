package com.sap.bulletinboard.ads.multitenancy.beans;

public class TenantContext {

    private final AvailableTenants availableTenants;

    private ThreadLocal<Tenant> tenantIdStorage = ThreadLocal.withInitial(() -> AvailableTenants.DEFAULT_TENANT);

    public TenantContext(AvailableTenants availableTenants) {
        this.availableTenants = availableTenants;
    }

    public Tenant getCurrentTenant() {
        return tenantIdStorage.get();
    }

    public void setCurrentTenantId(String tenantId) {
        Tenant tenant = availableTenants.getTenantForIdOrDefault(tenantId);
        tenantIdStorage.set(tenant);
    }

    public void clearCurrentTenant() {
        tenantIdStorage.remove();
    }

}