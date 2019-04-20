package com.sap.bulletinboard.ads.testutils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JwtGenerator {
    private static final String IDENTITY_ZONE_ID = "1e505bb1-2fa9-4d2b-8c15-8c3e6e6279c6";
    private static final String CLIENT_ID = "sb-bulletinboard!t895";

    // return a value suitable for the HTTP "Authorization" header containing the JWT with the given scopes
    public String getTokenForAuthorizationHeader(String... scopes) {
        return "Bearer " + getToken(scopes);
    }

    // return the JWT for the given scopes
    private String getToken(String... scopes) {
        return getToken(IDENTITY_ZONE_ID, scopes);
    }

    // return the JWT for the given scopes and tenant
    private String getToken(String tenantId, String... scopes) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        root.put("client_id", CLIENT_ID);
        root.put("exp", Integer.MAX_VALUE);
        root.set("scope", getScopesJSON(scopes));
        root.put("user_name", "user name");
        root.put("user_id", "D012345");
        root.put("email", "testUser@testOrg");
        root.put("zid", tenantId);

        return getTokenForClaims(root.toString());
    }

    // convert Java array into JSON array
    private ArrayNode getScopesJSON(String[] scopes) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode scopesArray = mapper.createArrayNode();
        for (String scope : scopes) {
            scopesArray.add(scope);
        }
        return scopesArray;
    }

    // sign the claims and return the resulting JWT
    private String getTokenForClaims(String claims) {
        RsaSigner signer = new RsaSigner(readFromFile("/privateKey.txt"));
        return JwtHelper.encode(claims, signer).getEncoded();
    }

    public String getPublicKey() {
        String publicKey = readFromFile("/publicKey.txt");
        return removeLinebreaks(publicKey);
    }

    private String removeLinebreaks(String input) {
        return input.replace("\n", "").replace("\r", "");
    }

    private String readFromFile(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            return IOUtils.toString(is);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public String getClientId() {
        return CLIENT_ID;
    }

    public String getIdentityZone() {
        return IDENTITY_ZONE_ID;
    }
}