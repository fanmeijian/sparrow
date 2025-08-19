package cn.sparrowmini.common.service.client.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class DefaultTokenExtractor implements AuthenticationTokenExtractor {

    @Override
    public boolean supports(Authentication authentication) {
        return authentication instanceof JwtAuthenticationToken ||
                authentication instanceof BearerTokenAuthentication;
    }

    @Override
    public String extractToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        if (authentication instanceof BearerTokenAuthentication bearerAuth) {
            return bearerAuth.getToken().getTokenValue();
        }
        return null;
    }
}