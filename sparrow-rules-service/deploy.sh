mvn clean package -Dmaven.test.skip=true
scp -l 8192 ./target/rebate-0.0.1-SNAPSHOT.jar root@sportunione.cn:/root/services/
ssh root@sportunione.cn "cd services;./rebate-service.sh"