package com.sap.bulletinboard.ads.services;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sap.hcp.cf.logging.common.LogContext;

@Component
public class StatisticsServiceClient {

    public static final String MSG_HEADER_TENANT = "tenant";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String routingKey; // Queue Name
    private final String exchange;
    private final RabbitTemplate rabbitTemplate;

    public StatisticsServiceClient(@Value("${bulletinboard.rabbit.adIsShown.exchange}") String exchangeName,
            @Value("${bulletinboard.rabbit.adIsShown.queue_increaseViewCount}") String queueName, AmqpAdmin amqpAdmin,
            RabbitTemplate rabbitTemplate) {

        this.routingKey = queueName;
        this.exchange = exchangeName;
        this.rabbitTemplate = rabbitTemplate;

        declarePubSubBinding(amqpAdmin);

        setupCallbacks();
    }

    private void declarePubSubBinding(AmqpAdmin amqpAdmin) {
        DirectExchange directExchange = new DirectExchange(exchange); // default: durable, no auto-delete
        amqpAdmin.declareExchange(directExchange);

        Map<String, Object> arguments = null; // TODO try to configure time to live (x-message-ttl, Integer in ms)
        Queue queue = new Queue(routingKey, true, false, false, arguments);
        amqpAdmin.declareQueue(queue);

        Binding binding = BindingBuilder.bind(queue).to(directExchange).withQueueName();
        amqpAdmin.declareBinding(binding);
    }

    public void advertisementIsShown(Long id, String tenant) throws RuntimeException {
        send(id, tenant);
    }

    private void setupCallbacks() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                logger.warn("message with id {} was not acknowledged, and that's why sent again: {}",
                        correlationData.getId(), cause);
                // right now it would end up into an infinite loop!!!
                // rabbitTemplate.send(exchange, routingKey, ((CorrelationDataWithMessage)
                // correlationData).getMessage());
            } else {
                logger.info("message with correlation-id {} was successfully acknowledged", correlationData.getId());
            }
        });

        // only called for mandatory messages
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            // right now it would end up into an infinite loop!!!
            // rabbitTemplate.send(exchange, routingKey, message);
            logger.error(
                    "message '{}' returned; couldn't be successfully routed to exchange '{}' with routingKey '{}': {}({})",
                    msg, exchange, routingKey, replyText, replyCode);
        });

        /*
         * Replace the correlation data with one containing the converted message in case we want to resend it after a
         * nack (not acknowledgment).
         */
        rabbitTemplate.setCorrelationDataPostProcessor((message, correlationData) -> new CorrelationDataWithMessage(
                message.getMessageProperties().getCorrelationId(), message));
    }

    public void send(Long id, String tenant) throws RuntimeException {
        logger.info("sending message '{}' for routing key '{}'", id, routingKey);

        rabbitTemplate.convertAndSend(exchange, routingKey, id, message -> {
            message.getMessageProperties().setCorrelationId(LogContext.getCorrelationId());
            message.getMessageProperties().setHeader(MSG_HEADER_TENANT, tenant);
            message.getMessageProperties().setMessageId(String.valueOf(message.hashCode())); // for retry
            return message;
        }); // the correlationData is specified in setCorrelationDataPostProcessor callback
    }

    public static class CorrelationDataWithMessage extends CorrelationData {
        private final Message message;

        public CorrelationDataWithMessage(String messageId, Message message) {
            super(messageId);
            this.message = message;
        }

        public String getTenant() {
            assert (message.getMessageProperties().getHeaders().containsKey(MSG_HEADER_TENANT));
            return (String) message.getMessageProperties().getHeaders().get(MSG_HEADER_TENANT);
        }

        public Message getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "CorrelationData [id=" + this.getId() + ", tenant=" + this.getTenant() + ", message=" + this.message
                    + "]";
        }
    }
}