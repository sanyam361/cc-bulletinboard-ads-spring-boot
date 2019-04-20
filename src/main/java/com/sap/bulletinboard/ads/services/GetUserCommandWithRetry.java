package com.sap.bulletinboard.ads.services;

import static com.sap.hcp.cf.logging.common.LogContext.HTTP_HEADER_CORRELATION_ID;

import java.util.Collections;
import java.util.function.Function;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sap.bulletinboard.ads.models.User;
import com.sap.hcp.cf.logging.common.LogContext;

public class GetUserCommandWithRetry extends HystrixCommand<User> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int BACK_OFF_PERIOD = 100;
    private static final int EXECUTION_TIMEOUT = 2500;

    private final String id;
    private final String correlationId;
    private final String url;
    private final RestTemplate restTemplate;
    private final Function<GetUserCommandWithRetry, User> fallbackFunction;
    private final RetryTemplate retryTemplate;

    GetUserCommandWithRetry(String url, RestTemplate restTemplate,
            Function<GetUserCommandWithRetry, User> fallbackFunction) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("User"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("User.getById")).andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(EXECUTION_TIMEOUT)));

        this.id = RandomStringUtils.randomAlphanumeric(10);
        this.correlationId = LogContext.getCorrelationId();
        this.url = url;
        this.restTemplate = restTemplate;
        this.fallbackFunction = fallbackFunction;
        this.retryTemplate = createRetryTemplate();
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(
                new SimpleRetryPolicy(2, Collections.singletonMap(HttpServerErrorException.class, Boolean.TRUE)));
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(BACK_OFF_PERIOD);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    @Override
    protected User run() {
        LogContext.initializeContext(correlationId);

        logger.info("sending request {} with timeout {}", url, getTimeoutInMs());
        try {
            User response = retryTemplate.execute(retryContext -> getResponseEntity().getBody());
            logger.info("received HTTP response successfully");
            return response;
        } catch (HttpServerErrorException error) {
            logger.warn("received server HTTP status code: {}", error.getStatusCode());
            throw error;
        } catch (HttpClientErrorException error) {
            logger.error("received client HTTP status code: {}", error.getStatusCode());
            throw new HystrixBadRequestException("Unsuccessful outgoing request due to client error", error);
        } catch (RuntimeException error) {
            logger.error("runtime failure: " + error.getMessage());
            throw new HystrixBadRequestException("Unsuccessful outgoing request due to runtime error", error);
        }
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

    protected ResponseEntity<User> getResponseEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HTTP_HEADER_CORRELATION_ID, correlationId);
        HttpEntity<User> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
    }

    private int getTimeoutInMs() {
        Integer timeoutMs = this.properties.executionTimeoutInMilliseconds().get();
        return timeoutMs != null ? timeoutMs.intValue() : 500;
    }

}