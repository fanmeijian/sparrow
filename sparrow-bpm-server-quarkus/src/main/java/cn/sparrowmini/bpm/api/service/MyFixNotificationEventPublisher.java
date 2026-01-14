package cn.sparrowmini.bpm.api.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;

@ApplicationScoped
public class MyFixNotificationEventPublisher implements EventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(MyFixNotificationEventPublisher.class.getName());
    private static final String CHANNEL_NAME = "kogito-deadline-events";
    @Inject
    @Channel(CHANNEL_NAME)
    Emitter<DataEvent<?>> emitter;

    public MyFixNotificationEventPublisher() {
    }

    public void publish(DataEvent<?> event) {
        if (event.getType().contains("Deadline")) {
            logger.debug("About to publish event {} to topic {}", event, CHANNEL_NAME);

            try {
                this.emitter.send(event);
                logger.debug("Successfully published event {} to topic {}", event, CHANNEL_NAME);
            } catch (Exception var3) {
                Exception e = var3;
                logger.error("Error while publishing event to topic {} for event {}", new Object[]{CHANNEL_NAME, event, e});
            }
        } else {
            logger.debug("Unknown type of event '{}', ignoring", event.getType());
        }

    }

    @Override
    public void publish(Collection<DataEvent<?>> events) {
        Iterator var2 = events.iterator();

        while(var2.hasNext()) {
            DataEvent<?> event = (DataEvent)var2.next();
            this.publish(event);
        }
    }
}