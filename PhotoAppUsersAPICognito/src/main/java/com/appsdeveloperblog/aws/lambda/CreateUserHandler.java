package com.appsdeveloperblog.aws.lambda;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.appsdeveloperblog.aws.lambda.service.CognitoUserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

/**
 * Handler for requests to Lambda function.
 */
public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final CognitoUserService cognitoUserService = new CognitoUserService("ap-southeast-1");
    private final String clientId = System.getenv("COGNITO_CLIENT_ID");
    private final String clientSecretId = System.getenv("COGNITO_CLIENT_SECRET_ID");
    private final Logger loggerContext = LoggerFactory.getLogger(CreateUserHandler.class);
    public CreateUserHandler() {

    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input,
                                                      final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        // Handle request body from request api gateway
        String requestBody = input.getBody();
        LambdaLogger logger = context.getLogger();

        logger.log("requestBody: " + requestBody);

        JsonObject userDetail = JsonParser.parseString(requestBody).getAsJsonObject();

        try{
            JsonObject createUser = cognitoUserService.createUser(userDetail, clientId, clientSecretId);
            response.withStatusCode(200);
            response.withBody(new Gson().toJson(createUser, JsonObject.class));
        }catch (AwsServiceException ex) {
            logger.log("Error: " + ex.getMessage());
            response.withStatusCode(500);
            response.withBody("{ \"message\": \"Error: " + ex.getMessage() + "\" }");
        }

        return response;
    }

}
