//package cn.sparrowmini.bpm.api.service;
//
//import jakarta.inject.Inject;
//import org.eclipse.microprofile.reactive.messaging.Channel;
//import org.eclipse.microprofile.reactive.messaging.Emitter;
//import org.kie.kogito.event.DataEvent;
//import org.kie.kogito.event.EventPublisher;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Collection;
//import java.util.Iterator;
//
//public class JobEventNotification implements EventPublisher {
//    private static final Logger logger = LoggerFactory.getLogger(JobEventNotification.class.getName());
//    private static final String CHANNEL_NAME = "kogito-jobs-events";
//    @Inject
//    @Channel(CHANNEL_NAME)
//    Emitter<DataEvent<?>> emitter;
//
//    @Override
//    public void publish(DataEvent<?> event) {
//        if (event.getType().contains("JobEvent")) {
//            logger.debug("About to publish event {} to topic {}", event, CHANNEL_NAME);
//
//            try {
//                this.emitter.send(event);
//                logger.debug("Successfully published event {} to topic {}", event, CHANNEL_NAME);
//            } catch (Exception var3) {
//                Exception e = var3;
//                logger.error("Error while publishing event to topic {} for event {}", new Object[]{CHANNEL_NAME, event, e});
//            }
//        } else {
//            logger.debug("Unknown type of event '{}', ignoring", event.getType());
//        }
//    }
//
//    @Override
//    public void publish(Collection<DataEvent<?>> events) {
//        Iterator var2 = events.iterator();
//
//        while(var2.hasNext()) {
//            DataEvent<?> event = (DataEvent)var2.next();
//            this.publish(event);
//        }
//    }
//}
