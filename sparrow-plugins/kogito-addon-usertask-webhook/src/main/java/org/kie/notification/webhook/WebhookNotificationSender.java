package org.kie.notification.webhook;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kie.kogito.event.usertask.UserTaskInstanceDeadlineDataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceDeadlineEventBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ApplicationScoped
public class WebhookNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(WebhookNotificationSender.class);

    @ConfigProperty(name = "notification.webhook.url")
    String webhookUrl;

    @ConfigProperty(name = "notification.webhook.token", defaultValue = "")
    String token;

    @Inject
    @RestClient
    WebhookClient webhookClient;

    public void send(Map<String, Object> event) {
        UserTaskInstanceDeadlineEventBody data = (UserTaskInstanceDeadlineEventBody) event;
        WebhookPayload payload = new WebhookPayload(
                data.getUserTaskInstanceId(),
                data.getUserTaskName(),
                data.getNotification().toString(),
                data.getEventUser()
        );
        webhookClient.send(webhookUrl, token, payload)
                .subscribe().with(
                        r -> log.info("Webhook notified: {}", data.getUserTaskInstanceId()),
                        e -> log.error("Webhook notification failed", e)
                );
    }
}