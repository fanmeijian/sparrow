package cn.sparrowmini.bpm.server.process;

import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessStartedEvent;
import org.springframework.stereotype.Component;

@Component
public class RestrictStartProcessListener extends DefaultProcessEventListener {

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        String processId = event.getProcessInstance().getProcessId();

        // 拦截指定流程
//        if (FORBIDDEN_PROCESSES.contains(processId)) {
//            String username = getCurrentUsername();
//            List<String> userRoles = getCurrentUserRoles();
//
//            boolean authorized = userRoles.stream().anyMatch(ALLOWED_ROLES::contains);
//            if (!authorized) {
//                throw new SecurityException("User [" + username + "] is not allowed to start process " + processId);
//            }
//        }
    }


}