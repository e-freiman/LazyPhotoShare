package service;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import service.googlephoto.GooglePhotoProxy;
import service.googlephoto.GooglePhotoSecret;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceTest {

    private GooglePhotoProxy googlePhotoProxy = new GooglePhotoProxy();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Mock
    private SecretRetriever secretRetriever;

    private Service service;

    @Before
    public void setUp() {

        when(secretRetriever.getGooglePhotoSecret()).thenReturn(new GooglePhotoSecret());

        service = new Service(secretRetriever, googlePhotoProxy, gson);
    }

    @Test
    public void successfulResponse() throws IOException {
        //service.serve(mock(Context.class), mock((LambdaLogger.class)));
    }
}
