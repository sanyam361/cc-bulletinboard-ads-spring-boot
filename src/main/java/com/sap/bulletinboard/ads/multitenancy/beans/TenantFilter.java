package com.sap.bulletinboard.ads.multitenancy.beans;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.hcp.cf.logging.common.LogContext;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;

public class TenantFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TenantContext tenantContext;

    public TenantFilter(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    private static final String TENANT_ID = "tenantId";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String tenantId = readTenantIdFromJWT();

        LogContext.add(TENANT_ID, tenantId);
        logger.debug("Set current tenantId to: {}", tenantId);
        tenantContext.setCurrentTenantId(tenantId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            tenantContext.clearCurrentTenant();
            logger.debug("Removed current tenantId: {}", tenantId);
            LogContext.remove(TENANT_ID);
        }
    }

    private String readTenantIdFromJWT() {
        String tenantId = null;
        try {
            UserInfo userInfo = SecurityContext.getUserInfo();
            tenantId = userInfo.getIdentityZone();
        } catch (UserInfoException e) {
            logger.error("UserInfoException, no tenant could be determined for this request.", e);
        }
        return tenantId;
    }

    @Override
    public void destroy() {

    }
}