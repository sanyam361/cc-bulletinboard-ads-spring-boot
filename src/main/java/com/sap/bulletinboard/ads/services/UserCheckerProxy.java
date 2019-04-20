package com.sap.bulletinboard.ads.services;

public class UserCheckerProxy implements UserChecker {

    private final UserChecker enabledChecker;
    private final UserChecker disabledChecker;

    private volatile boolean enabled = true;

    public UserCheckerProxy(UserChecker enabledChecker, UserChecker disabledChecker) {
        this.enabledChecker = enabledChecker;
        this.disabledChecker = disabledChecker;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    @Override
    public boolean checkUser(String userId) {
        return currentChecker().checkUser(userId);
    }

    private UserChecker currentChecker() {
        return enabled ? enabledChecker : disabledChecker;
    }

}
