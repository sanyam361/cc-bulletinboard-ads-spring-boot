package com.sap.bulletinboard.ads.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sap.bulletinboard.ads.models.User;

@RunWith(MockitoJUnitRunner.class)
public class GetUserCommandWithRetryTest {

    private static final User FALLBACK_USER = new User();

    private final String url = "test";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<User> restResponseEntity;

    private GetUserCommandWithRetry command;

    @Before
    public void setup() {
        Hystrix.reset();

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(User.class)))
                .thenReturn(restResponseEntity);
        command = new GetUserCommandWithRetry(url, restTemplate, cmd -> FALLBACK_USER);
    }

    @Test
    public void getUserCommandRunSuccessfullyAtFirstAttempt() {
        User expectedUser = new User();
        when(restResponseEntity.getBody()).thenReturn(expectedUser);

        User actualUser = command.execute();

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    public void getUserCommandRunSuccessfullyAtSecondAttemptAfterSingleServerError() {
        User expectedUser = new User();
        when(restResponseEntity.getBody()).thenThrow(serverError()).thenReturn(expectedUser);
        User actualUser = command.execute();
        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test(expected = HystrixBadRequestException.class)
    public void getUserCommandWithClientErrorFails() {
        when(restResponseEntity.getBody()).thenThrow(clientError());
        command.execute();
    }

    @Test(expected = HystrixBadRequestException.class)
    public void getUserCommandWithRuntimeErrorFails() {
        when(restResponseEntity.getBody()).thenThrow(new RuntimeException());
        command.execute();
    }

    @Test
    public void getUserCommandWithRepeatedServerErrorTriggersFallback() {
        when(restResponseEntity.getBody()).thenThrow(serverError());
        User actualUser = command.execute();
        assertThat(actualUser).isEqualTo(FALLBACK_USER);
    }

    @Test
    public void getUserCommandWithTimeoutRetry() {
        when(restResponseEntity.getBody()).thenThrow(serverError());
        ConfigurationManager.getConfigInstance()
                .setProperty("hystrix.command.User.getById.execution.isolation.thread.timeoutInMilliseconds", 50);
        command.execute();
        assertThat(command.getExecutionEvents()).contains(HystrixEventType.TIMEOUT);
    }

    private HttpServerErrorException serverError() {
        return new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
    }

    private HttpClientErrorException clientError() {
        return new HttpClientErrorException(HttpStatus.BAD_REQUEST);
    }

}