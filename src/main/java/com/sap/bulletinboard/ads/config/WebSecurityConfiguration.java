package com.sap.bulletinboard.ads.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.xs2.security.commons.SAPOfflineTokenServicesCloud;

@Configuration
public class WebSecurityConfiguration {

    // configure offline verification which checks if any provided JWT was properly signed
    @Bean
    protected SAPOfflineTokenServicesCloud offlineTokenServices() {
        return new SAPOfflineTokenServicesCloud();
    }

    @Bean
    public SystemPropertyHelper systemPropertyHelper(
            @Value("${vcap.services.uaa-bulletinboard.credentials.xsappname}") String xsAppName) {
        return new SystemPropertyHelper(xsAppName);
    }

    public class SystemPropertyHelper {

        private String tenantIndex;

        /**
         * The environment variable contains the tenant index e.g. "xsappname": "bulletinboard!t500". Note that
         * "uaa-bulletinboard" needs to match the service name, which is specified in the manifest.
         */
        public SystemPropertyHelper(String xsAppName) {
            this.tenantIndex = xsAppName.replaceAll(WebSecurityConfigAdapter.REGEX_TENANT_INDEX, "");
        }

        public String getTenantIndex() {
            return tenantIndex;
        }

        private String getScope(String localScope) {
            return tenantIndex + "." + localScope;
        }

        public String getDisplayScope() {
            return getScope(WebSecurityConfigAdapter.DISPLAY_SCOPE);
        }

        public String getUpdateScope() {
            return getScope(WebSecurityConfigAdapter.UPDATE_SCOPE);
        }
    }

}