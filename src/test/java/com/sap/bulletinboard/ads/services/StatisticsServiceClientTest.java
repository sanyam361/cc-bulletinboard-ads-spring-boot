package com.sap.bulletinboard.ads.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.rabbitmq.client.Channel;
import com.sap.bulletinboard.ads.config.RabbitConfig;
import com.sap.bulletinboard.ads.config.TestServicesConfiguration;
import com.sap.hcp.cf.logging.common.LogContext;

@ContextConfiguration(classes = { RabbitConfig.class, TestServicesConfiguration.class })
@RunWith(SpringRunner.class)
public class StatisticsServiceClientTest {
    private static final String routingKey = "testQueue";
    private static final String exchange = "testX";
    private final String correlationIdFromHeader = "corellation-id-1234";

    @SpyBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @MockBean
    private Channel channel;

    @Captor
    ArgumentCaptor<Message> messageArgumentCaptor;

    private StatisticsServiceClient statisticsServiceClient;

    @Before
    public void setUp() {
        LogContext.initializeContext(correlationIdFromHeader);
        statisticsServiceClient = new StatisticsServiceClient(exchange, routingKey, amqpAdmin, rabbitTemplate);
    }

    @Test
    public void validateMessage() {
        Long id = 555L;
        String tenant = "tenant-xyz";

        Mockito.doNothing().when(rabbitTemplate).send(Mockito.anyString(), Mockito.anyString(),
                Mockito.nullable(Message.class), Mockito.nullable(CorrelationData.class));
        statisticsServiceClient.advertisementIsShown(id, tenant);
        verify(rabbitTemplate).send(eq(exchange), eq(routingKey), messageArgumentCaptor.capture(),
                Mockito.nullable(CorrelationData.class));

        assertEquals(correlationIdFromHeader,
                messageArgumentCaptor.getValue().getMessageProperties().getCorrelationId());
        assertEquals(tenant, messageArgumentCaptor.getValue().getMessageProperties().getHeaders()
                .get(StatisticsServiceClient.MSG_HEADER_TENANT));
        assertEquals(id.toString(), new String(messageArgumentCaptor.getValue().getBody()));
    }
}
