package cn.sparrowmini.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages={"cn.sparrowmini.identity.local","cn.sparrowmini.rbac.persistence.jpa","cn.sparrowmini.group.persistence.jpa","cn.sparrowmini.datapolicy.persistence.jpa","cn.sparrowmini.system.dictionary","cn.sparrowmini.system.audit","cn.sparrowmini.system.presentation","cn.sparrowmini.system.configuration","cn.sparrowmini.system.navigation","cn.sparrowmini.file"})
public class AdminApplication {
    public static void main(String[] args){SpringApplication.run(AdminApplication.class,args);}
}
