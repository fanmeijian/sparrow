version=0.3.0
mvn clean package -Dmaven.test.skip=true
scp -l 8192 ./target/keycloak-wechatmini-$version.jar root@sportunione.cn:/usr/local/keycloak-21.1.1/providers/keycloak-wechat.jar
#ssh root@sportunione.cn "cd /usr/local/keycloak-21.1.1/bin;./start.sh"