package com.sap.bulletinboard.ads.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sap.bulletinboard.ads.models.User;

/**
 * This is a learning test for Hystrix and to test the GetUserCommand implementation.
 */
public class GetUserCommandTest {
    private static final User FALLBACK_USER = new User();
    private static final User USER = new User();

    @After
    public void tearDown() {
        Hystrix.reset(); // to clear out all HystrixPlugins, allowing you to set a new one
    }

    @Test
    public void responseReturnedSynchronously() {
        TestableUserCommand command = new TestableUserCommand(this::dummyUser).respondWithOkUser();
        User user = command.execute();
        assertThat(user, is(USER));
    }

    @Test
    public void responseReturnedAsynchronously() throws Exception {
        TestableUserCommand command = new TestableUserCommand(this::dummyUser).respondWithOkUser();
        User user = command.queue().get();
        assertThat(user, is(USER));
    }

    @Test
    public void responseTimedOutFallback() {
        TestableUserCommand command = new TestableUserCommand(this::dummyUser).provokeTimeout();
        User user = command.execute();
        assertThat(user, is(FALLBACK_USER));
    }

    @Test
    public void responseErrorFallback() {
        TestableUserCommand command = new TestableUserCommand(this::dummyUser).respondWithError();
        User user = command.execute();
        assertThat(user, is(FALLBACK_USER));
    }

    @Test(expected = HystrixBadRequestException.class)
    public void responseHystrixBadRequest() {
        TestableUserCommand command = new TestableUserCommand(this::dummyUser).respondWithBadRequest();
        User user = null;
        try {
            user = command.execute();
        } finally {
            assertThat(user, is(nullValue())); // fallback is not be called in case of HystrixBadRequestException
        }
    }

    // useful for optional exercise step
    private User dummyUser(GetUserCommand command) {
        return FALLBACK_USER;
    }

    // This command implementation does not send network requests, but instead behaves as configured using the
    // responseWith methods.
    private static class TestableUserCommand extends GetUserCommand {
        private boolean provokeTimeout;
        private ResponseEntity<User> responseEntity;
        private RestClientException exception;

        TestableUserCommand(Function<GetUserCommand, User> fallbackFunction) {
            super("", null, fallbackFunction);
        }

        TestableUserCommand respondWithOkUser() {
            responseEntity = ResponseEntity.ok(USER);
            return this;
        }

        TestableUserCommand respondWithError() {
            exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            return this;
        }

        TestableUserCommand respondWithBadRequest() {
            exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            return this;
        }

        TestableUserCommand provokeTimeout() {
            provokeTimeout = true;
            return this;
        }

        @Override
        protected ResponseEntity<User> getResponseEntity() {
            if (exception != null) {
                throw exception;
            }
            if (provokeTimeout) {
                try {
                    Thread.sleep(getTimeoutInMs() + 20);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            return responseEntity;
        }
    }
}