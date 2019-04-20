package com.sap.bulletinboard.ads.services;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StatisticsListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public StatisticsListener(@Value("${bulletinboard.rabbit.statistics.queue_periodical}") String queueName,
            AmqpAdmin amqpAdmin, ConnectionFactory rabbitConnectionFactory) {
        amqpAdmin.declareQueue(new Queue(queueName));

        logger.info("registering as listener for for queue '{}'", queueName);
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        listenerContainer.setConnectionFactory(rabbitConnectionFactory);
        listenerContainer.setQueueNames(queueName);
        listenerContainer.setShutdownTimeout(2000);
        listenerContainer.setReceiveTimeout(500);
        listenerContainer.setMessageListener(this);
        listenerContainer.start();
    }

    @Override
    public void onMessage(Message message) {
        logger.info("got statistics: {}", toString(message));
    }

    private String toString(Message message) {
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }
}