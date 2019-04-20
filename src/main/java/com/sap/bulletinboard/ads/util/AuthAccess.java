package com.sap.bulletinboard.ads.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.sap.bulletinboard.ads.config.WebSecurityConfiguration;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;

/**
 * Provides uniform access to request authorization context and user details.
 */
@RequestScope
@Component("authAccess")
public class AuthAccess {
    private UserInfo userInfo;
    private Authentication auth;

    private boolean isAnonymous;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private WebSecurityConfiguration.SystemPropertyHelper securityPropertyHelper;

    public AuthAccess(WebSecurityConfiguration.SystemPropertyHelper securityPropertyHelper) {
        this.auth = SecurityContextHolder.getContext().getAuthentication();
        this.isAnonymous = this.auth instanceof AnonymousAuthenticationToken;
        this.securityPropertyHelper = securityPropertyHelper;

        if (!this.isAnonymous) {
            try {
                this.userInfo = SecurityContext.getUserInfo();
            } catch (UserInfoException exc) {
                logger.warn("User Info can't be determined due to " + exc.toString());
                throw new RuntimeException(exc.getLocalizedMessage());
            }
        }
    }

    public String getName() {
        return auth.getName();
    }

    public String getEmail() {
        if (userInfo != null) {
            try {
                return userInfo.getEmail();
            } catch (UserInfoException exc) {
                return null;
            }
        }
        return null;
    }

    public boolean hasUpdateScope() {
        return hasScope(securityPropertyHelper.getUpdateScope());
    }

    public boolean hasDisplayScope() {
        return hasScope(securityPropertyHelper.getDisplayScope());
    }

    private boolean hasScope(String scope) {
        if (userInfo != null) {
            try {
                return userInfo.checkScope(scope);
            } catch (UserInfoException exc) {
                logger.warn("Local scope could not be checked due to " + exc.toString());
                return false;
            }
        }
        return false;
    }
}
