package com.sap.bulletinboard.ads.services;

import static com.sap.bulletinboard.ads.services.UserServiceClient.RESOURCE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.Hystrix;
import com.sap.bulletinboard.ads.config.RetryConfig;
import com.sap.bulletinboard.ads.models.User;

@RunWith(SpringRunner.class)
@RestClientTest({ UserServiceClient.class })
@AutoConfigureWebClient(registerRestTemplate = true)
// UserServiceClient uses @RefreshScope, therefore we need to enable auto configuration for this test
// furthermore, we need to manually enable Spring Retry for this type of test
@ImportAutoConfiguration({ RetryConfig.class, RefreshAutoConfiguration.class })
@TestPropertySource(properties = { "USER_ROUTE=test" })
public class UserServiceClientTest {

    @Value("${USER_ROUTE}")
    private String userServiceRoute;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private MockRestServiceServer server;

    @Before
    public void setup() {
        Hystrix.reset(); // avoid hystrix crcuit breaker metrics side-effects across tests
    }

    @Test
    @Ignore
    public void isPremiumUser() {
        server.expect(requestTo(queryEndpointForUser("42"))).andRespond(withUser(false));
        server.expect(requestTo(queryEndpointForUser("43"))).andRespond(withUser(true));

        assertThat(userServiceClient.isPremiumUser("42")).isFalse();
        assertThat(userServiceClient.isPremiumUser("43")).isTrue();
        server.verify();
    }

    @Test
    @Ignore
    public void isPremiumUserWithSuccessfulRetry() {
        server.expect(requestTo(queryEndpointForUser("42"))).andRespond(withServerError());
        server.expect(requestTo(queryEndpointForUser("42"))).andRespond(withUser(true));

        assertThat(userServiceClient.isPremiumUser("42")).isTrue();
        server.verify();
    }

    @Test
    @Ignore
    public void isPremiumUserWithUnsuccessfulRetry() {
        server.expect(times(2), requestTo(queryEndpointForUser("42"))).andRespond(withServerError());

        assertThat(userServiceClient.isPremiumUser("42")).isFalse();
        server.verify();
    }

    @Test(expected = HttpClientErrorException.class)
    @Ignore
    public void isPremiumUserFailingDueToClientError() {
        server.expect(requestTo(queryEndpointForUser("42"))).andRespond(withUnauthorizedRequest());
        userServiceClient.isPremiumUser("42");
    }

    private String queryEndpointForUser(String id) {
        return userServiceRoute + "/" + RESOURCE_NAME + "/" + id;
    }

    private DefaultResponseCreator withUser(boolean isPremium) {
        return withSuccess(userAsJson(isPremium), MediaType.APPLICATION_JSON_UTF8);
    }

    private String userAsJson(boolean premium) {
        User user = new User();
        user.setPremiumUser(premium);
        try {
            return new ObjectMapper().writeValueAsString(user);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}