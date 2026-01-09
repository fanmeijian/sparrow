package cn.sparrowmini.server.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

@Configuration
public class DefaultWebSecurityConfig {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                .authorizeHttpRequests(matcherRegistry -> {
                            matcherRegistry.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ 放行预检请求
                                    .requestMatchers("/v3/**", "/swagger-ui/**", "/h2-console/**", "/login/**", "/oauth2/**", "/oauth2/authorization/keycloak", "/").permitAll();
//                                    .anyRequest().authenticated();
                            // allow forwarded access to zul under class path web resource folder

                            //permit the URL for login and logout
                            matcherRegistry.requestMatchers(AntPathRequestMatcher.antMatcher("/login"), AntPathRequestMatcher.antMatcher("/logout")).permitAll();
                            // configure secure pages
//                            matcherRegistry.requestMatchers(AntPathRequestMatcher.antMatcher("/secure")).hasRole("USER");
                            //enforce all requests to be authenticated, just permit some paths configured below
                            matcherRegistry.anyRequest().authenticated();
                        }


                )
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/secure/main"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                ).cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable); // ✅ 关闭 CSRF（如适用）;

        return http.build();
    }

    /**
     * 创建一个处理 OIDC 登出的处理器
     * 成功退出后，会重定向到 Keycloak 的登出端点，再跳回我们的首页
     */
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // 设置登出后的重定向地址（通常是应用首页）
        // 注意：这个 URL 必须在 Keycloak Client 的 "Valid Post Logout Redirect URIs" 中配置
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");

        return oidcLogoutSuccessHandler;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);

//		corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList(HttpMethod.OPTIONS.name(), HttpMethod.GET.name(),
                HttpMethod.HEAD.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.PUT.name(),
                HttpMethod.PATCH.name()));
        corsConfiguration.applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("*")); // 或具体前端地址
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//        config.setMaxAge(3600L); // 可选：缓存 preflight 响应 1 小时
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("*")); // 可以根据需要改成前端地址
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        return source;
//    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(@Value("${keycloak.resource}") String clientId) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Realm roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            // Client roles
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
                Map<String, Object> client = (Map<String, Object>) resourceAccess.get(clientId);
                if (client.containsKey("roles")) {
                    List<String> clientRoles = (List<String>) client.get("roles");
                    for (String role : clientRoles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }

            return authorities;
        });
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

}
