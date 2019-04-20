package com.sap.bulletinboard.ads.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.hcp.cf.logging.servlet.filter.RequestLoggingFilter;

@Configuration
public class LoggerConfiguration {
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> filterRegistrationBean() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();
        registrationBean.setFilter(requestLoggingFilter);
        return registrationBean;
    }

}
