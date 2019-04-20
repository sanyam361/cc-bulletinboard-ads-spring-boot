package com.sap.bulletinboard.ads.services;

import static com.sap.hcp.cf.logging.common.LogContext.HTTP_HEADER_CORRELATION_ID;

import java.util.function.Function;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sap.bulletinboard.ads.models.User;
import com.sap.hcp.cf.logging.common.LogContext;

public class GetUserCommand extends HystrixCommand<User> {

    private static int timeoutInMilliseconds = 1000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String id;
    private final Function<GetUserCommand, User> fallbackFunction;
    private final String correlationId;
    private final String url;
    private final RestTemplate restTemplate;

    GetUserCommand(String url, RestTemplate restTemplate, Function<GetUserCommand, User> fallbackFunction) {

        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("User"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("User.getById")).andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(2500)));

        ConfigurationManager.getConfigInstance().setProperty(
                "hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", timeoutInMilliseconds);

        this.id = RandomStringUtils.randomAlphanumeric(10);
        this.fallbackFunction = fallbackFunction;
        this.url = url;
        this.restTemplate = restTemplate;
        this.correlationId = LogContext.getCorrelationId();
    }

    @Override
    protected User run() {
        LogContext.initializeContext(this.correlationId);

        logger.info("sending request {} with timeout {}", url, getTimeoutInMs());
        try {
            User response = getResponseEntity().getBody();
            logger.info("received HTTP response successfully");
            return response;
        } catch (HttpClientErrorException error) {
            logger.error("received HTTP status code: {}", error.getStatusCode());
            throw new HystrixBadRequestException("Unsuccessful outgoing request", error);
        } catch (HttpServerErrorException error) {
            logger.warn("received HTTP status code: {}", error.getStatusCode());
            throw error;
        }
    }

    protected ResponseEntity<User> getResponseEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HTTP_HEADER_CORRELATION_ID, correlationId);
        HttpEntity<User> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
    }

    int getTimeoutInMs() {
        Integer timeoutMs = this.properties.executionTimeoutInMilliseconds().get();
        return timeoutMs != null ? timeoutMs.intValue() : 500;
    }

    @Override
    protected User getFallback() {
        if (isResponseTimedOut()) {
            logger.error("[RestRequestID={}] execution timed out after {}ms (HystrixCommandKey:{})", id,
                    getTimeoutInMs(), this.commandKey);
        }
        if (isFailedExecution()) {
            logger.error("[RestRequestID={}] execution failed", id, getFailedExecutionException());
        }
        if (isResponseRejected()) {
            logger.warn("[RestRequestID={}] request was rejected", id);
        }

        if (fallbackFunction != null) {
            return fallbackFunction.apply(this);
        }
        return super.getFallback();
    }

    public static void setTimeout(int timeout) {
        GetUserCommand.timeoutInMilliseconds = timeout;
    }
}