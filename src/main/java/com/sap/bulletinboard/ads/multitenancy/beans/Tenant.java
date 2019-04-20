package com.sap.bulletinboard.ads.multitenancy.beans;

public class Tenant {

    private final String tenantId;
    private final String schemaName;

    public Tenant(String tenantId, String schemaName) {
        this.tenantId = tenantId;
        this.schemaName = schemaName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getSchemaName() {
        return schemaName;
    }

}