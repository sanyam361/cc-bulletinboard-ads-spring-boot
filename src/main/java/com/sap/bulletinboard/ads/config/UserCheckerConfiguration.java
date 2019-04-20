package com.sap.bulletinboard.ads.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.bulletinboard.ads.services.UserChecker;
import com.sap.bulletinboard.ads.services.UserCheckerActive;
import com.sap.bulletinboard.ads.services.UserCheckerDisabled;
import com.sap.bulletinboard.ads.services.UserCheckerProxy;
import com.sap.bulletinboard.ads.services.UserServiceClient;

@Configuration
public class UserCheckerConfiguration {

    @Bean
    public UserCheckerProxy userChecker(UserServiceClient userServiceClient) {
        UserChecker enabledChecker = new UserCheckerActive(userServiceClient);
        UserChecker disabledChecker = new UserCheckerDisabled();
        UserCheckerProxy userCheckerProxy = new UserCheckerProxy(enabledChecker, disabledChecker);
        return userCheckerProxy;
    }

}
