package org.kie.notification.webhook;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kie.kogito.event.usertask.UserTaskInstanceDeadlineDataEvent;
import org.kie.kogito.usertask.UserTaskEventListener;
import org.kie.kogito.usertask.events.UserTaskAssignmentEvent;

import java.util.Map;

@ApplicationScoped
public class UserTaskWebhookListener implements UserTaskEventListener {

    @Inject
    WebhookNotificationSender webhookSender;


    @Override
    public void onUserTaskAssignment(UserTaskAssignmentEvent event) {
        // 自定义通知逻辑
        String assignee = event.getEventUser();
        System.out.println("发送自定义通知给：" + assignee);
        webhookSender.send(Map.of());
        // 示例：调用短信、Webhook、Slack 等
        // smsService.send(assignee, "您有新任务：" + task.getName());
    }

}