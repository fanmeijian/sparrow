启动参数说明：
-Djava.security.egd=file:/dev/./urandom 
need to add this else will case java.lang.IllegalArgumentException: Class BOOT-INF.classes.cn.sparrowmini.bpm.server.config.DefaultWebSecurityConfig not found in the projec

-Dorg.kie.server.bypass.auth.user=true
启用spring security认证的用户身份

-Dkie.maven.settings.custom=./settings.xml
启用MAVEN仓库的地址配置，主要用来查找kjar的位置

-Dorg.jbpm.task.cleanup.enabled=false
流程结束后，不会清理掉已经处理的任务

-Dorg.kie.deployment.desc.location=file:./kie-deployment-descriptor-prd.xml
指定部署文档，定义workitemhandler等

增加流程变量返回的时候会加上类名的问题处理，

增加时间处理格式
-Dorg.kie.server.json.format.date="true"
-Dorg.kie.server.json.date_format="yyyy-MM-dd hh:mm:ss.SSSZ"；

本地环境调试注意：
在idea中，若此项目是作为module设置的，则需将bpm-application-service.xml和settings.xml以及descriptor.xml拷贝到project的根目录，否则系统启动的时候将找不到这几个文件；因为在idea里面project.dir是读取idea project的路径，而不是module的路径

-Dorg.jbpm.var.log.length="20000"
设置流程变量的长度，同时需要修改数据库表 variableinstancelog的oldvalue和value字段为对应的长度

两种方式：
1）与workbench集成，由workbench进行发布
2）本地仓库，由kie server处理，这个时候kie server作为独立运行的服务器



docker build -f src/main/docker/Dockerfile -t sparrow-bpm-server:1.1 .
docker compose -p dengbo -f src/main/docker/docker-compose.yaml up -d --force-recreate


SPRING_APPLICATION_JSON
SPRING_PROFILES_ACTIVE

docker run -e "SPRING_PROFILES_ACTIVE=prd" -e SPRING_APPLICATION_JSON="{\"server.port\":8080,\"server.servlet.context-path\":\"/dengbo-bpm\",\"spring.datasource.url\":\"jdbc:postgresql://{host}:{port}/{db}\",\"spring.datasource.username\":\"{db-user}\",\"spring.datasource.password\":\"{password}\",\"spring.jpa.properties.hibernate.dialect\":\"org.hibernate.dialect.PostgreSQLDialect\",\"keycloak.auth-server-url\":\"{keycloak-url}\",\"keycloak.realm\":\"{realm-name}\",\"keycloak.resource\":\"{client-id}\",\"keycloak.credentials.secret\":\"{client-secret}\"}" -d --name=dengbo-bpm -p 8091:8080 -t sparrow-bpm-server:1.0 


services:
  dengbo-bpm:
    image: sparrow-bpm-server:1.0
    ports: 
    volumes:
      - type: bind
        source: ./static
        target: /opt/app/static
volumes:
  myapp: