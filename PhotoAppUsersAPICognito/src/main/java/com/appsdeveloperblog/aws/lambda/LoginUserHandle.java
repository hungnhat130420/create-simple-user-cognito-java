package com.appsdeveloperblog.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.appsdeveloperblog.aws.lambda.service.CognitoUserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.HashMap;
import java.util.Map;

public class LoginUserHandle implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CognitoUserService cognitoUserService = new CognitoUserService("ap-southeast-1");

    private static final String clientId = "5i0jif7iaf8te7d3bie55e9koo";
    private static final String clientSecretId = "15gdk8vajets3r4esige02homgqjpq5s02na5u26mgh5k23nnro4";

    public LoginUserHandle() {
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            JsonObject loginUserDetails = JsonParser.parseString(apiGatewayProxyRequestEvent.getBody()).getAsJsonObject();
            JsonObject loginUserSuccess = cognitoUserService.loginUser(loginUserDetails, clientId, clientSecretId);
            response.withStatusCode(200);
            response.withBody(new Gson().toJson(loginUserSuccess, JsonObject.class));
        } catch (AwsServiceException e) {
            response.withStatusCode(500);
            response.withBody("{ \"message\": \"Error: " + e.getMessage() + "\" }");
        }

        return response;
    }
}
