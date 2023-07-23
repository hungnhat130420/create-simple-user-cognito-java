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

public class ConfirmUserCognitoHandle implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final CognitoUserService cognitoUserService = new CognitoUserService("ap-southeast-1");

    private static final String clientId = "COGNITO_CLIENT_ID";
    private static final String clientSecretId = "COGNITO_CLIENT_SECRET_ID";

    public ConfirmUserCognitoHandle() {

    }
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input,
                                                      Context context){
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        //get email from request body
        String requestBodyJsonString = input.getBody();
        JsonObject jsonObject = JsonParser.parseString(requestBodyJsonString).getAsJsonObject();
        String email = jsonObject.get("email").getAsString();
        String confirmationCode = jsonObject.get("code").getAsString();

        //confirm user from cognito
        try{
            JsonObject confirmUserResult = cognitoUserService.confirmUserSignup(clientId, clientSecretId, email, confirmationCode);
            response.withStatusCode(200);
            response.withBody(new Gson().toJson(confirmUserResult, JsonObject.class));
        }catch (AwsServiceException ex) {
            response.withStatusCode(500);
            response.withBody("{ \"message\": \"Error: " + ex.getMessage() + "\" }");
        }

        return response;
    }
}
