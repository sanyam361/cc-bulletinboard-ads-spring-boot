package com.sap.bulletinboard.ads.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import com.sap.bulletinboard.ads.config.WebSecurityConfiguration.SystemPropertyHelper;
import com.sap.bulletinboard.ads.multitenancy.beans.TenantContext;
import com.sap.bulletinboard.ads.multitenancy.beans.TenantFilter;

@EnableWebSecurity
@EnableResourceServer
public class WebSecurityConfigAdapter extends ResourceServerConfigurerAdapter {

    static final String DISPLAY_SCOPE = "Display";
    static final String UPDATE_SCOPE = "Update";
    static final String REGEX_TENANT_INDEX = "(\\!t\\d+)?";

    private final WebSecurityConfiguration.SystemPropertyHelper propertyHelper;
    private final TenantContext tenantContext;

    public WebSecurityConfigAdapter(SystemPropertyHelper propertyHelper, TenantContext tenantContext) {
        this.propertyHelper = propertyHelper;
        this.tenantContext = tenantContext;
    }

    // configure Spring Security, demand authentication and specific scopes
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            // register TenantFilter in the chain after the SecurityContext is made available by the respective filter
            .addFilterAfter(new TenantFilter(tenantContext), SecurityContextHolderAwareRequestFilter.class)
            .sessionManagement()
                // session is created by approuter
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()
            // demand specific scopes depending on intended request
            .authorizeRequests()
                // enable OAuth2 checks
                .antMatchers("/**/*.js", "/**/*.json", "/**/*.xml", "/**/*.html").permitAll()
                .antMatchers(POST, "/api/v1/ads/**").access(hasScope(UPDATE_SCOPE))
                .antMatchers(PUT, "/api/v1/ads/**").access(hasScope(UPDATE_SCOPE))
                .antMatchers(DELETE, "/api/v1/ads/**").access(hasScope(UPDATE_SCOPE))
                .antMatchers(GET, "/api/v1/ads/**").access(hasScope(DISPLAY_SCOPE))
                .antMatchers("/actuator/health").permitAll() // should not be exposed in production
                .antMatchers("/hystrix.stream").permitAll()
                .antMatchers("/test").permitAll() // TODO remove -> see DefaultController
                .antMatchers("/").permitAll()
                .anyRequest().denyAll(); // deny anything not configured above
        // @formatter:on
    }

    private String hasScope(String localScope) {
        // http://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/provider/expression/OAuth2SecurityExpressionMethods.html
        String scopeWithOrWithoutTenant = propertyHelper.getTenantIndex() + REGEX_TENANT_INDEX + "." + localScope;
        return "#oauth2.hasScopeMatching('" + scopeWithOrWithoutTenant + "')";
    }

}