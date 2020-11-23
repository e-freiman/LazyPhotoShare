package service;

import java.io.IOException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Handler for requests to Lambda function.
 */
public class Handler implements RequestHandler<ScheduledEvent, String> {

    private Injector injector = Guice.createInjector(new GuiceModule());
    private Service service = injector.getInstance(Service.class);

    @Override
    public String handleRequest(ScheduledEvent event, Context context) {
        final LambdaLogger logger = context.getLogger();
        logger.log(String.format("Input event: %s", event.toString()));
        try {
            service.serve(context, logger);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return "200 OK";
    }
}
