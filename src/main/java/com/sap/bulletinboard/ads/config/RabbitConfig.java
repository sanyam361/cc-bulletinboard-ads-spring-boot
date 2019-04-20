package com.sap.bulletinboard.ads.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter) {

        // TODO: Somehow, when the cloud profile is active, the properties below are not read from the
        // application.properties files (or overridden). So we set them programmatically here.
        verifyConnectionFactory(factory);

        RabbitTemplate template = new RabbitTemplate(factory);

        /* Workaround till spring-rabbit 2.0 release */
        // DefaultMessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
        // messagePropertiesConverter
        // .setCorrelationIdAsString(DefaultMessagePropertiesConverter.CorrelationIdPolicy.STRING);
        // template.setMessagePropertiesConverter(messagePropertiesConverter);
        /* Workaround end */

        template.setMessageConverter(messageConverter);
        template.setMandatory(true); // otherwise we get no info whether message could not be routed
        template.setReplyTimeout(3000);
        return template;
    }

    private void verifyConnectionFactory(ConnectionFactory factory) {
        if (factory instanceof CachingConnectionFactory) {
            CachingConnectionFactory ccFactory = (CachingConnectionFactory) factory;

            ccFactory.setPublisherConfirms(true);
            ccFactory.setPublisherReturns(true);
            ccFactory.setChannelCacheSize(100);

            Assert.isTrue(ccFactory.isPublisherConfirms(), "ConnectionFactory.publisherConfirms needs to be set");
            Assert.isTrue(ccFactory.isPublisherReturns(), "ConnectionFactory.publisherReturns needs to be set");
        } else {
            throw new IllegalStateException(
                    "RabbitConfig.java: Connection factory must be a CachingConnectionFactory.");
        }
    }
}
