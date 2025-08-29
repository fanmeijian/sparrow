package cn.sparrowmini.bpm.server.workitem;

import org.jbpm.services.api.service.ServiceRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceTaskRegisterConfig {

    public ServiceTaskRegisterConfig(HttpServiceTaskHandler httpServiceTaskHandler) {
        ServiceRegistry.get().register("HttpServiceTask", httpServiceTaskHandler);
    }


}
