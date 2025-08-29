servicename=bpm-server
envname=test
mvn package spring-boot:repackage -Dmaven.test.skip=true
mv ./target/*.jar /opt/services/$servicename.jar
pidlist=``
#mc
pidlist=`ps -ef|grep java|grep "spring.profiles.active=$envname /opt/services/$servicename"|grep -v "grep"|awk '{print $2}'`
for pid in ${pidlist}
{
    kill -9 $pid
    echo "KILL $servicename $pid:"
}

logfile=$servicename-$(date +'%Y-%m-%d')
nohup java -jar -Dorg.kie.deployment.desc.location=/opt/configurations/bpm-application-service.xml -Dorg.kie.server.json.format.date="true" -Dorg.kie.server.bypass.auth.user=true -Dkie.maven.settings.custom=/opt/configurations/settings.xml -Dorg.jbpm.task.cleanup.enabled=false -Dspring.profiles.active=$envname /opt/services/$servicename.jar --spring.application.json="$(</opt/configurations/$servicename.application-$envname.json)" >/var/log/services/$logfile-$envname.log 2>&1 &
tail -f /var/log/services/$logfile-$envname.log