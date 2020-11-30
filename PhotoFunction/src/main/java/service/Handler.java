package service;

import java.io.IOException;
import java.util.Collections;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Handler for requests to Lambda function.
 */
public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private Injector injector = Guice.createInjector(new GuiceModule());
    private Service service = injector.getInstance(Service.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        final LambdaLogger logger = context.getLogger();
        try {
            service.serve(event, context, logger);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Collections.emptyMap())
                .withBody("{\"message\":\"success\"}");
    }
}
