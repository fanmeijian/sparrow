version=bpm-server-1.0-SNAPSHOT
mvn package spring-boot:repackage -Dmaven.test.skip=true
scp -l 8192 ./target/$version.jar root@111.230.49.197:/opt/
#ssh root@111.230.49.197 "cd /opt;./startservice.sh $version"
