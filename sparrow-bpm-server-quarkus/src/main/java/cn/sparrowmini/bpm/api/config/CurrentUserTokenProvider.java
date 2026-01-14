//package cn.sparrowmini.bpm.api.config;
//
//import io.quarkus.oidc.AccessTokenCredential;
//import io.quarkus.security.identity.SecurityIdentity;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//
//import java.util.Map;
//import java.util.Optional;
//
//@ApplicationScoped
//public class CurrentUserTokenProvider {
//
//    @Inject
//    SecurityIdentity securityIdentity;
//
//
//    public String getAccessToken() {
//
//        // 尝试从 SecurityIdentity 拿
//        Map<String, Object> map = securityIdentity.getAttributes();
//        AccessTokenCredential credentials = (AccessTokenCredential) securityIdentity.getCredentials().iterator().next();
//        Optional<String> tokenOpt = Optional.ofNullable((String) credentials.getToken());
//        if (tokenOpt.isPresent()) {
//            String token = tokenOpt.get();
//            if (isExpired(token)) {
//                return refreshToken();
//            }
//            return token;
//        }
//        throw new RuntimeException("No access token found for current user");
//    }
//
//    private boolean isExpired(String token) {
//        // 简单判断 JWT 是否过期，可用 JwtClaims 检查 exp
//        return false; // 这里可实现真正的检查
//    }
//
//    private String refreshToken() {
//        // 可调用 Keycloak Refresh Token API 或使用 OIDC Client 自动刷新
//        // 这里示例直接抛异常
//        throw new RuntimeException("Token expired, refresh logic not implemented yet");
//    }
//}
