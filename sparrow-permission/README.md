# sparrow-permission
将原来的组织权限拆出来独立的项目

本地构建，不通过jenkins
mvn clean package -DskipTests
docker build -f src/main/docker/Dockerfile -t sparrow-permission-service:1.0 .
docker compose up -d


services:
  dengbo-service:
    image: sparrow-permission-service:1.0
    container_name: toupiao-permission-service
    environment:
      TZ: Asia/Shanghai
      SPRING_APPLICATION_JSON: >
        {
          "server.port": 8080,
          "server.servlet.context-path": "/sparrow-permission-service",
          "spring.datasource.url": "jdbc:mysql://{host}:{port}/{dbname}",
          "spring.datasource.username": "",
          "spring.datasource.password": "",
          "spring.servlet.multipart.max-file-size":"50MB",
          "spring.servlet.multipart.max-request-size":"100MB",
          "spring.jpa.properties.hibernate.dialect": "org.hibernate.dialect.MySQLDialect",
          "spring.jpa.properties.hibernate.jdbc.time_zone":"Asia/Shanghai",
          "keycloak.auth-server-url": "https://keycloak.linkair-tech.cn",
          "spring.datasource.driver-class-name": "com.mysql.cj.jdbc.Driver",
          "keycloak.realm": "{realm}",
          "keycloak.resource": "{client-id}",
          "keycloak.credentials.secret": "{client-secret}"
        }
      SPRING_PROFILES_ACTIVE: prd
    ports:
      - "8035:8080"
    volumes:
      - /etc/localtime:/etc/localtime:ro
      - /etc/timezone:/etc/timezone:ro
