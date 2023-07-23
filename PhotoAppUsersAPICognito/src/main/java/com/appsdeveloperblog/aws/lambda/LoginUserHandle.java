package com.appsdeveloperblog.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.appsdeveloperblog.aws.lambda.service.CognitoUserService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class LoginUserHandle implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CognitoUserService cognitoUserService = new CognitoUserService("us-east-1");

    private static final String clientId = "COGNITO_CLIENT_ID";
    private static final String clientSecretId = "COGNITO_CLIENT_SECRET_ID";

    public LoginUserHandle() {
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        JsonObject loginUserDetails = JsonParser.parseString(apiGatewayProxyRequestEvent.getBody()).getAsJsonObject();
        cognitoUserService.loginUser(loginUserDetails, clientId, clientSecretId);
        return response;
    }
}
