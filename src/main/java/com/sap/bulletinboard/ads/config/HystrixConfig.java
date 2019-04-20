package com.sap.bulletinboard.ads.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;

@Configuration
public class HystrixConfig {
    @Bean
    public ServletRegistrationBean<HystrixMetricsStreamServlet> servletRegistrationBean() {
        return new ServletRegistrationBean<>(new HystrixMetricsStreamServlet(), "/hystrix.stream");
    }
}
