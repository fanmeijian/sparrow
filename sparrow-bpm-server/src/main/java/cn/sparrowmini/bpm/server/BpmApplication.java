package cn.sparrowmini.bpm.server;

import cn.sparrowmini.bpm.server.util.MyMVELActionBuilder;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.builder.dialect.mvel.MVELProcessDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;


//@Import({JbpmExtServiceConfiguration.class})
@SpringBootApplication
public class BpmApplication {

    public static void main(String[] args) {
//        MVELProcessDialect.setActionbuilder(new MyMVELActionBuilder());
        SpringApplication.run(BpmApplication.class, args);

    }

}