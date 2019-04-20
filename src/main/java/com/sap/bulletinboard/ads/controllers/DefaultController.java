package com.sap.bulletinboard.ads.controllers;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@RestController
public class DefaultController {

    private static final String BEARER = "Bearer";

    @GetMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String get(@RequestHeader("Authorization") String authorization) {

        // TODO DO NEVER EXPOSE THIS DATA IN PRODUCTION!!!

        if (!authorization.isEmpty() && authorization.startsWith(BEARER)) {

            String tokenContent = authorization.replaceFirst(BEARER, "").trim();

            // Decode JWT token
            Jwt decodedJwt = JwtHelper.decode(tokenContent);

            return decodedJwt.getClaims();
        }
        return JSONObject.quote("OK");
    }

}