package vadc.heartbeat.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static okhttp3.RequestBody.create;

public class PushNotificationService {

    private static final String FIREBASE_API_URL = "https://fcm.googleapis.com/v1/projects/heartbeatpublisher/messages:send";

    private static final MediaType JSON = MediaType.parse("application/json; UTF-8");

    private OkHttpClient firebaseHttpClient;

    public PushNotificationService(OkHttpClient firebaseHttpClient) {

        this.firebaseHttpClient = firebaseHttpClient;
    }

    public String send(String body, Context context) {
        Request request = new Request.Builder()
                .url(FIREBASE_API_URL)
                .post(create(JSON, buildNotificationMessage(body)))
                .build();
        try {
            firebaseHttpClient.newCall(request).execute();
            logSuccess(body, context);
            return "OK " + body;
        } catch (IOException e) {
            logFailure(body, context);
            throw new RuntimeException(e);
        }
    }

    private void logSuccess(Object body, Context context) {
        if (context != null) {
            context.getLogger().log(String.format("Pushed event %s", body));
        }
    }

    private void logFailure(Object body, Context context) {
        if (context != null) {
            context.getLogger().log(String.format("Failed to push event %s", body));
        }
    }

    private static String buildNotificationMessage(String event) {
        JsonObject jNotification = new JsonObject();
        String title = event.substring(0, event.indexOf(']') + 1);
        jNotification.addProperty("title", title);
        String body = event.substring(event.indexOf(']') + 1);
        jNotification.addProperty("body", body);

        JsonObject jMessage = new JsonObject();
        jMessage.add("notification", jNotification);
        jMessage.addProperty("topic", "notification_events");

        JsonObject jFcm = new JsonObject();
        jFcm.add("message", jMessage);

        return jFcm.toString();
    }

}
