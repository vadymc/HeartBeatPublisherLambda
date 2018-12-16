package vadc.heartbeat;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import vadc.heartbeat.config.FirebaseConfig;
import vadc.heartbeat.service.PushNotificationService;

import java.util.ArrayList;
import java.util.List;

public class PushRequestHandler implements RequestHandler<SQSEvent, List<String>> {

    private PushNotificationService pushNotificationService;

    public PushRequestHandler() {
        FirebaseConfig firebaseConfig = new FirebaseConfig();
        this.pushNotificationService = new PushNotificationService(firebaseConfig.firebaseHttpClient());
    }

    @Override
    public List<String> handleRequest(SQSEvent input, Context context) {
        List<String> result = new ArrayList<>();
        input.getRecords().forEach(msg -> result.add(pushNotificationService.send(msg.getBody(), context)));
        if (context != null) {
            context.getLogger().log(String.format("Pushed event [%s]", input));
        }
        return result;
    }
}
