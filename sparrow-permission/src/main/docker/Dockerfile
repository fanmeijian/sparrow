FROM openjdk:17-alpine
MAINTAINER Sowrd <fanmeijian@gmail.com>

#WORKDIR /opt/services
# copy resource folder
COPY target/*.jar /opt/services/application.jar

#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/services/application.jar"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /opt/services/application.jar"]
