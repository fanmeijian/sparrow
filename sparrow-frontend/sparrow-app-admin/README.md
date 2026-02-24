ng build --configuration=docker
docker build -t sparrow-app-admin:1.0 .
docker compose up -d


version: "3"
services:
  dengbo-admin:
    image: sparrow-app-admin:1.0
    container_name: dengbo-app-admin
    ports:
      - "8202:80"
    environment:
      - API_BASE=http://localhost:8081/dengbo-service
      - PEM_BASE=http://localhost:8081/dengbo-service
      - REALM=dengbo
      - CLIENT_ID=dengbo-web
      - KEYCLOAK_API_BASE=https://keycloak.linkair-tech.cn
