FROM adoptopenjdk/openjdk11
MAINTAINER Sowrd <fanmeijian@gmail.com>

#WORKDIR /opt/services
# copy resource folder
COPY target/*.jar /opt/services/application.jar
ENV JAVA_OPTS=""
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/services/application.jar"]
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/services/application.jar"]
ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /opt/services/application.jar
#ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /opt/services/application.jar"]