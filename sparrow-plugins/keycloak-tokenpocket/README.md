# keycloak 微信小程序第三方登录

在keycloak的providers的目录下，realm名字.properties格式
carboss-prd.properties
AUTH_URL=http://localhost:4200/#/tokenpocket-login //登录地址
TOKEN_URL=http://localhost:4200/#/tokenpocket-sign //钱包签名地址
SING_MSG=sign request //签名内容
VERIFY_SIGN=http://localhost:8510/car-boss-service/user-endpoints/verify-sign //验证签名的地址
对 keycloak 21.1.1 版本实现了微信小程序登录。

优先获取用户 unionid 作为 keycloak 用户 username(即 keycloak 中的唯一标识)，如果没有，则以 openid 作为唯一标识
参考链接https://www.keycloak.org/docs/latest/securing_apps/#direct-naked-impersonation
# 使用方式

1. 部署和启动

把包复制到keycloak的providers文件夹
./kc.sh start-dev --http-port 7980 --features=preview
注意：生产环境下要先执行kc.sh build才能扫描识别出部署的插件


2. 设置首次登录后，不补充资料
   Authentication->first broker login-> Review Profile 设置为disable


3. 根据用户名获取token
   Users-> Permissions->impersonate -> client details-> policies -> create policy-> Client
   然后回到Users-> Permissions->impersonate，把policy一览设置自己刚刚创建的的policy即可，其他默认。
   在Clients->Client details->Policy details，选中client-impersonlators，在clients里面加上需要通过用户名获取token的client id（因为不需要用户的密码就可以获取）

4. 调用获取token
   curl -X POST \
   -d "client_id=starting-client" \
   -d "client_secret=the client secret" \
   --data-urlencode "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
   -d "requested_subject=username" \
   http://localhost:8080/realms/myrealm/protocol/openid-connect/token


