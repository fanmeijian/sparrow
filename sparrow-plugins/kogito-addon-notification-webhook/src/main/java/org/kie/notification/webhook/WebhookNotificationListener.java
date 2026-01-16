package org.kie.notification.webhook;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kie.kogito.event.usertask.UserTaskInstanceDeadlineDataEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WebhookNotificationListener {

    @Inject
    WebhookNotificationSender webhookSender;

    @Incoming("kogito-deadline-consumer")
    public void onDeadline(UserTaskInstanceDeadlineDataEvent event) {
        //        log.info("Received Kogito usertask deadline event: {}", event.getData().getTaskName());
        webhookSender.send(event);
    }
}
