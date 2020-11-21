package service;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Handler for requests to Lambda function.
 */
public class Handler implements RequestHandler<Map<String,String>, String> {

    private Injector injector = Guice.createInjector(new GuiceModule());
    private Service service = injector.getInstance(Service.class);

    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        final LambdaLogger logger = context.getLogger();
        try {
            service.serve(context, logger);
        } catch (Exception e) {
            logger.log(e.getMessage());
            logger.log(e.getStackTrace().toString());
            return "500";
        }
        return "200 OK";
    }
}
