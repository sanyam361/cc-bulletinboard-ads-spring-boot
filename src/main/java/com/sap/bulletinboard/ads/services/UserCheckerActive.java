package com.sap.bulletinboard.ads.services;

public class UserCheckerActive implements UserChecker {

    private final UserServiceClient userServiceClient;

    public UserCheckerActive(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public boolean checkUser(String userId) {
        return userServiceClient.isPremiumUser(userId);
    }

}
