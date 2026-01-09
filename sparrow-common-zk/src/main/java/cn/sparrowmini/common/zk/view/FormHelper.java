package cn.sparrowmini.common.zk.view;

import org.zkoss.formbuilder.FormComposer;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.util.Notification;

public class FormHelper {
    static public EventListener eventListener = event -> {
        Notification.show(event.getData().toString());
    };

    static void subscribeFormSave(EventListener eventListener) {
        EventQueue queue = EventQueues.lookup(FormComposer.EVENT_QUEUE_NAME);
        queue.subscribe(eventListener);
    }
    static void showUseInput() {
        subscribeFormSave(eventListener);
    }
}
