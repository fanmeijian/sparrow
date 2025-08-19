package cn.sparrowmini.common.service.client.config;

import org.springframework.security.core.Authentication;

public interface IWebClientOAuth2Filter {
    public String extractAccessToken(Authentication auth);
}
