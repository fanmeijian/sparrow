//package cn.sparrowmini.bpm.api.service;
//
//import io.smallrye.reactive.messaging.annotations.Merge;
//import jakarta.enterprise.context.ApplicationScoped;
//import org.eclipse.microprofile.reactive.messaging.Incoming;
//import org.eclipse.microprofile.reactive.messaging.Message;
//import org.eclipse.microprofile.reactive.messaging.Outgoing;
//
//@ApplicationScoped
//public class MessageBridge {
//    @Incoming("kogito-deadline-events") // 对应出场
//    @Outgoing("kogito-deadline-consumer")     // 对应进场
//    @Merge
//    public Message<String> bridge(Message<String> msg) {
//        return msg;
//    }
//}