package com.sap.bulletinboard.ads.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.junit.BrokerRunning;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.sap.bulletinboard.ads.config.RabbitConfig;
import com.sap.bulletinboard.ads.config.TestServicesConfiguration;
import com.sap.hcp.cf.logging.common.LogContext;

@ContextConfiguration(classes = { RabbitConfig.class, TestServicesConfiguration.class })
@RunWith(SpringRunner.class)
public class StatisticsServiceClientCT {
    private static final String routingKey = "testQueue";
    private static final String exchange = ""; // Note: Test works only for Direct Exchange (exchange = "")
    private final String correlationIdFromHeader = "corellation-id-1234";
    private String tenant = "tenant-xyz";

    @ClassRule
    public static BrokerRunning brokerRunning = BrokerRunning.isRunningWithEmptyQueues(routingKey);

    @SpyBean // is prototype scoped, as we need a fresh one per thread
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Before
    public void setUp() {
        LogContext.initializeContext(correlationIdFromHeader);
    }

    @After
    public void tearDown() {
        brokerRunning.removeTestQueues();
    }

    @Test
    public void validateMessage() {
        Long id = 111L;

        StatisticsServiceClient statisticsServiceClient = new StatisticsServiceClient(exchange, routingKey, amqpAdmin,
                rabbitTemplate);
        statisticsServiceClient.advertisementIsShown(id, tenant);
        verify(rabbitTemplate).send(eq(exchange), eq(routingKey), messageArgumentCaptor.capture(),
                Mockito.any(CorrelationData.class));

        assertEquals(correlationIdFromHeader,
                messageArgumentCaptor.getValue().getMessageProperties().getCorrelationId());
        assertEquals(tenant, messageArgumentCaptor.getValue().getMessageProperties().getHeaders()
                .get(StatisticsServiceClient.MSG_HEADER_TENANT));
        assertEquals(id.toString(), new String(messageArgumentCaptor.getValue().getBody()));
    }

    @Test
    public void resendNackMessage() {
        Long id = 222L;
        String notExistingExchange = "testExchange";

        StatisticsServiceClient statisticsServiceClient = new StatisticsServiceClient(notExistingExchange, routingKey,
                amqpAdmin, rabbitTemplate);
        statisticsServiceClient.advertisementIsShown(id, tenant);
        verify(rabbitTemplate, times(1)).send(eq(notExistingExchange), eq(routingKey), messageArgumentCaptor.capture(),
                Mockito.any(CorrelationData.class)); // TODO should be called more than once

        // TODO how to verify that callback was executed?
    }

    @Test
    public void resendUnroutableMessage() {
        Long id = 333L;

        brokerRunning.deleteQueues(routingKey);

        StatisticsServiceClient statisticsServiceClient = new StatisticsServiceClient(exchange, routingKey, amqpAdmin,
                rabbitTemplate);
        statisticsServiceClient.advertisementIsShown(id, tenant);
        verify(rabbitTemplate, times(1)).send(eq(exchange), eq(routingKey), messageArgumentCaptor.capture(),
                Mockito.any(CorrelationData.class));

        // TODO how to verify that callback was executed?
    }
}
