package com.sap.bulletinboard.ads.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.SecuredAnnotationSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    @Value("${security.basic.enabled}")
    private boolean securityEnabled;

    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        return securityEnabled ? new SecuredAnnotationSecurityMetadataSource() : null;
    }
}
