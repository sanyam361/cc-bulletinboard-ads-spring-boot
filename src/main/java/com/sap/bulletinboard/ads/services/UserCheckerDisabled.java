package com.sap.bulletinboard.ads.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCheckerDisabled implements UserChecker {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean checkUser(String userId) {
        logger.info("Returning true without checking user with id {}", userId);
        return true;
    }
}
