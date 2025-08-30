package cn.sparrowmini.keycloak.tokenpocket;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.Urls;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <a href=
 * "https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=2639bbef696c2f1540dec98ed4d45bcca460dd86&lang=zh_CN">参考文档</a>
 *
 * @author yangjian
 */
public class TokenPocketIdentityProvider extends AbstractOAuth2IdentityProvider<OAuth2IdentityProviderConfig>
        implements SocialIdentityProvider<OAuth2IdentityProviderConfig> {

    // 第一步: 请求CODE
    public static String AUTH_URL = "http://localhost:4200/#/tokenpocket-login";
    public static String SING_MSG = "sign request";
    public static String VERIFY_SIGN = "http://localhost:8510/car-boss-service/user-endpoints/verify-sign";
    // 第二步: 通过code获取access_token
    public static String TOKEN_URL = "http://localhost:4200/#/tokenpocket-sign";
    public static final String DEFAULT_SCOPE = "openid profile email";


    public TokenPocketIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
        super(session, config);
//        config.setAuthorizationUrl(AUTH_URL);
//        config.setTokenUrl(TOKEN_URL);
        String realmName = session.getContext().getRealm().getName();
        AUTH_URL = ConfigHelper.getProperty(realmName, "AUTH_URL");
        TOKEN_URL = ConfigHelper.getProperty(realmName, "TOKEN_URL");
        SING_MSG = ConfigHelper.getProperty(realmName, "SING_MSG");
        VERIFY_SIGN = ConfigHelper.getProperty(realmName, "VERIFY_SIGN");
        System.out.println(String.format("{%s} {%s} {%s} {%s}",AUTH_URL,TOKEN_URL,SING_MSG,VERIFY_SIGN));
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        System.out.println("102----");
        return new Endpoint(callback, realm, event, this);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        String userId = getJsonProperty(profile, "data");
        BrokeredIdentityContext user = new BrokeredIdentityContext(userId.toLowerCase());
        user.setUsername(userId.toLowerCase());
        user.setBrokerUserId(userId.toLowerCase());
        user.setModelUsername(userId.toLowerCase());

        String nickname = getJsonProperty(profile, "nickname");
        user.setFirstName(nickname);
        user.setIdpConfig(getConfig());
        user.setIdp(this);
        user.setUserAttribute("avatar", getJsonProperty(profile, "headimgurl"));
        user.setUserAttribute("country", getJsonProperty(profile, "country"));
        user.setUserAttribute("province", getJsonProperty(profile, "province"));
        user.setUserAttribute("city", getJsonProperty(profile, "city"));
        user.setUserAttribute("sex", getJsonProperty(profile, "sex"));
        user.setUserAttribute("type", "wechat");
        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
        logger.info("保存用户信息到keycloak " + profile);
        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String response) {
        try {
            JsonNode profile = new ObjectMapper().readTree(response);
            BrokeredIdentityContext user = extractIdentityFromProfile(null, profile);
            return user;
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from github.", e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    public Response performLogin(AuthenticationRequest request) {
        try {

            String nonce = request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.NONCE_PARAM);
            request.getAuthenticationSession().setClientNote(OIDCLoginProtocol.NONCE_PARAM, nonce);

            String sss = TOKEN_URL
                    + "?redirect_uri="
                    + request.getRedirectUri()
                    + "&state123=" + request.getState().getEncoded();

            String loginHint = request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
            if (getConfig().isLoginHint() && loginHint != null) {
                sss = sss + "&login_hint=" + loginHint;
            }

            String prompt = getConfig().getPrompt();
            if (prompt == null || prompt.isEmpty()) {
                prompt = request.getAuthenticationSession().getClientNote(OAuth2Constants.PROMPT);
            }
            if (prompt != null) {
                sss = sss + "&prompt=" + prompt;
            }

            if (nonce == null || nonce.isEmpty()) {
                nonce = UUID.randomUUID().toString();
                request.getAuthenticationSession().setClientNote(OIDCLoginProtocol.NONCE_PARAM, nonce);
                sss = sss + "&nonce=" + nonce;

            }

            String acr = request.getAuthenticationSession().getClientNote(OAuth2Constants.ACR_VALUES);
            if (acr != null) {
                sss = sss + "&acr_values=" + acr;
            }

            return Response.seeOther(URI.create(sss)).build();

        } catch (Exception e) {
            throw new IdentityBrokerException("Could not create authentication request.", e);
        }
    }

    protected class Endpoint {

        protected final AuthenticationCallback callback;
        protected final RealmModel realm;
        protected final EventBuilder event;
        private final TokenPocketIdentityProvider provider;

        protected final KeycloakSession session;

        protected final ClientConnection clientConnection;

        protected final HttpHeaders headers;

        protected final HttpRequest httpRequest;

        @Context
        protected HttpRequest request;

        @Context
        protected UriInfo uriInfo;

        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event,
                        TokenPocketIdentityProvider provider) {
            this.callback = callback;
            this.realm = realm;
            this.event = event;
            this.provider = provider;
            this.session = provider.session;
            this.clientConnection = session.getContext().getConnection();
            this.httpRequest = session.getContext().getHttpRequest();
            this.headers = session.getContext().getRequestHeaders();
        }

        @GET
        public Response authResponse(@QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_STATE) String state,
                                     @QueryParam(AbstractOAuth2IdentityProvider.OAUTH2_PARAMETER_CODE) String authorizationCode,
                                     @QueryParam(OAuth2Constants.ERROR) String error,
                                     @QueryParam(OAuth2Constants.SCOPE_OPENID) String openid, @QueryParam("clientId") String clientId,
                                     @QueryParam("code") String sign,
                                     @QueryParam("tabId") String tabId) {
            KeycloakContext context = session.getContext();

            if (state == null) {
                return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_MISSING_STATE_ERROR);
            }
            try {
                AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
                context.setAuthenticationSession(authSession);
                OAuth2IdentityProviderConfig providerConfig = provider.getConfig();

                if (error != null) {
                    logger.error(error + " for broker login " + providerConfig.getProviderId());
                    if (error.equals(ACCESS_DENIED)) {
                        return callback.cancelled(providerConfig);
                    } else if (error.equals(OAuthErrorException.LOGIN_REQUIRED)
                            || error.equals(OAuthErrorException.INTERACTION_REQUIRED)) {
                        return callback.error(error);
                    } else {
                        return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
                    }
                }
//                System.out.println(msg + sign);
                if (authorizationCode != null) {
                    String address = "";
                    //解析用户的钱包地址
                    CloseableHttpResponse response;
                    String url = VERIFY_SIGN;
                    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                        HttpClientContext httpClientContext = new HttpClientContext();
                        HttpPost httpGet = new HttpPost(url);
                        httpGet.setEntity(new StringEntity("{\"msg\":\""+SING_MSG+"\",\"sign\":\"" + sign + "\"}", ContentType.APPLICATION_JSON));
                        response = httpClient.execute(httpGet, httpClientContext);
                        httpClientContext.getCookieStore().clear();
                        String responseStr = EntityUtils.toString(response.getEntity());
                        System.out.println("status: " + response.getStatusLine().getStatusCode() + " response: " + responseStr);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            address = responseStr;
                        } else {
                            throw new RuntimeException(String.format("{} {}", response.getStatusLine().getStatusCode(), responseStr));
                        }
                    }

                    BrokeredIdentityContext federatedIdentity = doGetFederatedIdentity(address);
                    federatedIdentity.setIdpConfig(providerConfig);
                    federatedIdentity.setIdp(provider);
                    federatedIdentity.setAuthenticationSession(authSession);
                    return callback.authenticated(federatedIdentity);
                }
            } catch (WebApplicationException e) {
                return e.getResponse();
            } catch (Exception e) {
                logger.error("Failed to make identity provider (token pocket) oauth callback", e);
            }
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
        }

        private Response errorIdentityProviderLogin(String message) {
            event.event(EventType.IDENTITY_PROVIDER_LOGIN);
            event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, message);
        }
    }
}
