package com.sap.bulletinboard.ads.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestServicesConfiguration {

    @Bean
    @Primary
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setPublisherConfirms(true);
        factory.setPublisherReturns(true);
        return factory;
    }

    @MockBean
    private AmqpAdmin amqpAdmin;

}
