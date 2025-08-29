servicename=bpm-server
envname=prd
mvn package spring-boot:repackage -Dmaven.test.skip=true
mv ./target/bpm-server-1.1.jar /opt/services/$servicename.jar
pidlist=``
#mc
pidlist=`ps -ef|grep java|grep "spring.profiles.active=$envname /opt/services/$servicename"|grep -v "grep"|awk '{print $2}'`
for pid in ${pidlist}
{
    kill -9 $pid
    echo "KILL $servicename $pid:"
}

logfile=$servicename-$(date +'%Y-%m-%d')
nohup java -jar -Dorg.kie.server.controller.user=workbench-server -Dorg.kie.server.controller.pwd=Pixcy2-qadcyx-kepguk -Dorg.jbpm.var.log.length="20000" -Dorg.kie.deployment.desc.location=file:/opt/configurations/kie-deployment-descriptor-$envname.xml -Dorg.kie.server.json.format.date="true" -Dorg.kie.server.bypass.auth.user=true -Dkie.maven.settings.custom=file:/opt/configurations/settings.xml -Dorg.jbpm.task.cleanup.enabled=false -Dspring.profiles.active=$envname /opt/services/$servicename.jar --spring.application.json="$(</opt/configurations/$servicename.application-$envname.json)" >/var/log/services/$logfile-$envname.log 2>&1 &
tail -f /var/log/services/$logfile-$envname.log
