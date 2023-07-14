package com.appsdeveloperblog.aws.lambda.service;

import com.google.gson.JsonObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/*
 Class này dùng để xử lý việc đăng kí user sau khi request thông qua Cognito
 gửi lên và đã được xử lý thông qua lambda trước
* */
public class CognitoUserService {
    private final CognitoIdentityProviderClient cognitoIdentityProviderClientClient;

    public CognitoUserService(String region) {
        this.cognitoIdentityProviderClientClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
    }
    public CognitoUserService(CognitoIdentityProviderClient cognitoClient) {

        this.cognitoIdentityProviderClientClient = cognitoClient;
    }

    public JsonObject createUser(JsonObject user, String clientId,
                                 String clientSecret) {

        // Lấy các thông tin user trong request gửi lên từ lambda
        // Get user infor sent from lambda
        String userId = UUID.randomUUID().toString();
        String email = user.get("email").getAsString();
        String password = user.get("password").getAsString();
        String firstName = user.get("firstName").getAsString();
        String lastName = user.get("lastName").getAsString();

        // Dùng để tạo các attribute cho user. Vì cognito có sẵn các attribute mặc định như email, phone, name
        // Used to create attributes for users. Because cognito has attributes like email, phone, name
        AttributeType attributeUserId = AttributeType.builder()
                .name("custom:userId")
                .value(userId)
                .build();

        AttributeType attributeName = AttributeType.builder()
                .name("name")
                .value(firstName + " " + lastName)
                .build();

        AttributeType attributeEmail = AttributeType.builder()
                .name("email")
                .value(email)
                .build();

        // Tạo list các attribute cho user
        // Create list attributes for user
        List<AttributeType> attributes = new ArrayList<>();
        attributes.add(attributeUserId);
        attributes.add(attributeName);
        attributes.add(attributeEmail);

        // Genere secret hash before sign up
        String generatedSecretHash = calculateSecretHash(clientId, clientSecret, email);

        // Sign up user to cognito
        SignUpRequest signUpRequest = SignUpRequest.builder().
                username(email).
                password(password).
                userAttributes(attributes).
                clientId(clientId).
                secretHash(generatedSecretHash).
                build();

        SignUpResponse signUpResponse = cognitoIdentityProviderClientClient.signUp(signUpRequest);
        JsonObject createUserResult = new JsonObject();

        // Init return type for lambda
        createUserResult.addProperty("isSuccessful", signUpResponse.sdkHttpResponse().isSuccessful());
        createUserResult.addProperty("statusCode", signUpResponse.sdkHttpResponse().statusCode());
        createUserResult.addProperty("cognitoUserId", signUpResponse.userSub());
        createUserResult.addProperty("userConfirmed", signUpResponse.userConfirmed());

        return createUserResult;
    }


    // Method for generating secret hash
    private static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating ");
        }
    }
}
