package cn.sparrowmini.permission.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "keycloak")
public class OAuthProperties {


    private String realm;
    private String resource;
    private Credential credentials;
    private String authServerUrl;

    @Data
    public static class Credential {
        private String id;
        private String secret;
        private String tokenUri;
    }
}