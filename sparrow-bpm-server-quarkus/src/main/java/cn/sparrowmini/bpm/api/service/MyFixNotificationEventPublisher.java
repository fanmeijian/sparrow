package cn.sparrowmini.bpm.api.service;
//
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.task.notification.quarkus.NotificationEventPublisher;

@Alternative
@Priority(1)
@ApplicationScoped
public class MyFixNotificationEventPublisher extends NotificationEventPublisher {
    @Inject
    @Channel("kogito-deadline-events")
    Emitter<DataEvent<?>> emitter;

    @Override
    public void publish(DataEvent<?> event) {
        // 修正逻辑：只要包含 Deadline 关键词，或者直接放行 UserTaskInstanceDeadlineDataEvent
        if (event.getType().contains("Deadline")) {
            // 复制父类的发送逻辑
            emitter.send(event);
        } else {
            super.publish(event); // 其他的交给父类（虽然父类也会忽略）
        }
    }
}