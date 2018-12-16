package vadc.heartbeat.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FirebaseConfig {

    private static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/firebase.messaging");

    public FirebaseConfig() {

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(getCredentialsStream()))
                    .setDatabaseUrl("https://heartbeatpublisher.firebaseio.com/")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FirebaseApp.initializeApp(options);
    }

    public OkHttpClient firebaseHttpClient() {
        try {
            return new OkHttpClient.Builder()
                    .addInterceptor(headersInterceptor(googleCredential())).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GoogleCredential googleCredential() throws IOException {
        GoogleCredential googleCredential = GoogleCredential
                .fromStream(getCredentialsStream())
                .createScoped(SCOPES);
        googleCredential.refreshToken();
        return googleCredential;
    }

    private Interceptor headersInterceptor(GoogleCredential googleCredential) {
        return chain -> {
            if (googleCredential.getExpiresInSeconds() < 60) {
                googleCredential.refreshToken();
            }

            Request request = chain.request();
            Request newRequest = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + googleCredential.getAccessToken())
                    .build();
            return chain.proceed(newRequest);
        };
    }


    private ByteArrayInputStream getCredentialsStream() {
        String firebaseKey = System.getenv("FIREBASE_KEY");
        return new ByteArrayInputStream(firebaseKey.getBytes());
    }
}
