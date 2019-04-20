package com.sap.bulletinboard.ads.multitenancy.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AvailableTenants {

    public static final Tenant DEFAULT_TENANT = new Tenant("public", "PUBLIC");

    private final List<Tenant> tenants;

    public AvailableTenants(List<String> tenantIds) {
        this.tenants = new ArrayList<>(tenantIds.size() + 1);
        for (String tenantId : tenantIds) {
            String schemaName = tenantId;
            // schemaName = schemaName.replaceAll("[^a-zA-Z]", "");
            // TODO: Using uppercase schema names is necessary because H2 does it by default, and not all libraries
            // quote the schema name so we run into "schema not found" problems.
            schemaName = schemaName.toUpperCase();
            Tenant tenant = new Tenant(tenantId, schemaName);
            tenants.add(tenant);
        }
        tenants.add(DEFAULT_TENANT);
    }

    public Tenant getTenantForIdOrDefault(String tenantId) {
        return tenants.stream().filter(t -> t.getTenantId().equals(tenantId)).findFirst().orElse(DEFAULT_TENANT);
    }

    public List<String> getAllSchemaNames() {
        return tenants.stream().map(Tenant::getSchemaName).collect(Collectors.toList());
    }

}
