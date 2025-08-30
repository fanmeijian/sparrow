package cn.sparrowmini.keycloak.wechatmini;

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
public class WechatIdentityProvider extends AbstractOAuth2IdentityProvider<OAuth2IdentityProviderConfig>
		implements SocialIdentityProvider<OAuth2IdentityProviderConfig> {

	// 第一步: 请求CODE
	public static final String AUTH_URL = "https://open.weixin.qq.com/connect/qrconnect";

	// 第二步: 通过code获取access_token
	public static final String TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

	// 应用授权作用域，拥有多个作用域用逗号（,）分隔，网页应用目前仅填写snsapi_login即可
	public static final String DEFAULT_SCOPE = "snsapi_login";

	public static final String WECHAT_AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";

	public static final String WECHAT_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

	public static final String WECHAT_DEFAULT_SCOPE = "snsapi_userinfo";

	// 第三步: 通过access_token调用 获取用户个人信息
	public static final String PROFILE_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

	public static final String OAUTH2_PARAMETER_CLIENT_ID = "appid";

	public static final String OAUTH2_PARAMETER_CLIENT_SECRET = "secret";

	public static final String WECHAT_APPID_KEY = "clientId2";

	public static final String WECHAT_APPID_SECRET = "clientSecret2";

	public static final String OPENID = "openid";

	public static final String WECHAT_USER_AGENT_FLAG = "micromessenger";

	public static String WECHAT_PUBLIC_ID = null; // 公众号
	public static String WECHAT_PUBLIC_SECRET = null; // 公众号

	public static String WECHAT_WEBSITE_ID = null; // 网站
	public static String WECHAT_WEBSITE_SECRET = null; // 网站

	public WechatIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
		super(session, config);
		config.setAuthorizationUrl(AUTH_URL);
		config.setTokenUrl(TOKEN_URL);
		String realmName = session.getContext().getRealm().getName();
		WECHAT_PUBLIC_ID = ConfigHelper.getProperty(realmName, "WECHAT_PUBLIC_ID");
		WECHAT_PUBLIC_SECRET = ConfigHelper.getProperty(realmName, "WECHAT_PUBLIC_SECRET");
		WECHAT_WEBSITE_ID = ConfigHelper.getProperty(realmName, "WECHAT_WEBSITE_ID");
		WECHAT_WEBSITE_SECRET = ConfigHelper.getProperty(realmName, "WECHAT_WEBSITE_SECRET");
		System.out.println(String.join("", "realmName=", realmName, "WECHAT_PUBLIC_ID=", WECHAT_PUBLIC_ID,
				"WECHAT_PUBLIC_SECRET=", WECHAT_PUBLIC_SECRET, "WECHAT_WEBSITE_ID=", WECHAT_WEBSITE_ID,
				"WECHAT_WEBSITE_SECRET", WECHAT_WEBSITE_SECRET));
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
		String unionid = getJsonProperty(profile, "unionid");
		String userId = unionid != null && unionid.length() > 0 ? unionid : getJsonProperty(profile, "openid");
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

	/**
	 * 设置联合token,返回用户profile context
	 *
	 * @param response
	 * @return
	 */
//    @Override
//    public BrokeredIdentityContext getFederatedIdentity(String response) {
//        String accessToken = extractTokenFromResponse(response, getAccessTokenResponseParameter());
//        if (accessToken == null) {
//            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
//        }
//        BrokeredIdentityContext context = doGetFederatedIdentity(response);
//        context.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);
//        return context;
//    }

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

	public Response performLogin(AuthenticationRequest request) {
		try {

			if (request.getHttpRequest().getHttpHeaders().getRequestHeader("code") != null
					&& request.getHttpRequest().getHttpHeaders().getRequestHeader("code").size() > 0) {
				String nonce = UUID.randomUUID().toString();
				UriBuilder uriBuilder = UriBuilder.fromUri(request.getRedirectUri());
				uriBuilder.queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded());
				uriBuilder.queryParam(OIDCLoginProtocol.NONCE_PARAM, nonce);
				request.getAuthenticationSession().setClientNote(OIDCLoginProtocol.NONCE_PARAM, nonce);

				String code = request.getHttpRequest().getHttpHeaders().getRequestHeader("code").get(0);

				uriBuilder.queryParam("code", code).queryParam("clientId", "wechat");
				uriBuilder.replaceQueryParam(OAUTH2_PARAMETER_REDIRECT_URI,
						request.getRedirectUri() + "?clientId=miniprogram");

				System.out.println("perform login 请求地址：" + uriBuilder.build().toString());

				return Response.seeOther(URI.create(uriBuilder.build().toString())).build();
			} else {

				String ua = request.getSession().getContext().getRequestHeaders().getHeaderString("user-agent")
						.toLowerCase();

				// 网页登录，扫二维码
				UriBuilder uriBuilderWechat = createAuthorizationUrl(request);

				if (isWechatBrowser(ua)) {
					// 微信浏览器登录
					uriBuilderWechat.replaceQueryParam("appid", WECHAT_PUBLIC_ID).queryParam("forcePopup", "true")
							.replaceQueryParam(OAUTH2_PARAMETER_REDIRECT_URI,
									request.getRedirectUri() + "?clientId=wechatbrowser");
				} else {
					uriBuilderWechat.replaceQueryParam("appid", WECHAT_WEBSITE_ID).queryParam("forcePopup", "true")
							.replaceQueryParam(OAUTH2_PARAMETER_REDIRECT_URI,
									request.getRedirectUri() + "?clientId=wechatsite");
				}

				URI authorizationUrl = uriBuilderWechat.build();

				System.out
						.println("perform login 请求地址 " + URI.create(authorizationUrl.toString() + "#wechat_redirect"));
				return Response.seeOther(URI.create(authorizationUrl.toString() + "#wechat_redirect")).build();

			}

		} catch (Exception e) {
			throw new IdentityBrokerException("Could not create authentication request.", e);
		}
	}

	protected String getDefaultScopes() {
		return DEFAULT_SCOPE;
	}

	/**
	 * 判断是否在微信浏览器里面请求
	 *
	 * @param ua 浏览器user-agent
	 * @return
	 */
	private boolean isWechatBrowser(String ua) {

		if (ua.indexOf(WECHAT_USER_AGENT_FLAG) > 0) {
			return true;
		}
		return false;
	}

	protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {

		final UriBuilder uriBuilder;
		String ua = request.getHttpRequest().getHttpHeaders().getHeaderString("user-agent").toLowerCase();
		if (isWechatBrowser(ua)) {
			// 是微信浏览器
			logger.info("----------wechat");
			uriBuilder = UriBuilder.fromUri(WECHAT_AUTH_URL);
			uriBuilder.queryParam(OAUTH2_PARAMETER_SCOPE, WECHAT_DEFAULT_SCOPE)
					.queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
					.queryParam(OAUTH2_PARAMETER_RESPONSE_TYPE, "code")
					.queryParam(OAUTH2_PARAMETER_CLIENT_ID, WECHAT_PUBLIC_ID)
					.queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());
		} else {
			// PC网站
			uriBuilder = UriBuilder.fromUri(getConfig().getAuthorizationUrl());
			uriBuilder.queryParam(OAUTH2_PARAMETER_SCOPE, getConfig().getDefaultScope())
					.queryParam(OAUTH2_PARAMETER_STATE, request.getState().getEncoded())
					.queryParam(OAUTH2_PARAMETER_CLIENT_ID, WECHAT_WEBSITE_ID)
					.queryParam(OAUTH2_PARAMETER_REDIRECT_URI, request.getRedirectUri());
		}

		String loginHint = request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
		if (getConfig().isLoginHint() && loginHint != null) {
			uriBuilder.queryParam(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
		}

		String prompt = getConfig().getPrompt();
		if (prompt == null || prompt.isEmpty()) {
			prompt = request.getAuthenticationSession().getClientNote(OAuth2Constants.PROMPT);
		}
		if (prompt != null) {
			uriBuilder.queryParam(OAuth2Constants.PROMPT, prompt);
		}

		String nonce = request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.NONCE_PARAM);
		if (nonce == null || nonce.isEmpty()) {
			nonce = UUID.randomUUID().toString();
			request.getAuthenticationSession().setClientNote(OIDCLoginProtocol.NONCE_PARAM, nonce);
		}
		uriBuilder.queryParam(OIDCLoginProtocol.NONCE_PARAM, nonce);

		String acr = request.getAuthenticationSession().getClientNote(OAuth2Constants.ACR_VALUES);
		if (acr != null) {
			uriBuilder.queryParam(OAuth2Constants.ACR_VALUES, acr);
		}
		return uriBuilder;
	}

	protected class Endpoint {

		protected final AuthenticationCallback callback;
		protected final RealmModel realm;
		protected final EventBuilder event;
		private final WechatIdentityProvider provider;

		protected final KeycloakSession session;

		protected final ClientConnection clientConnection;

		protected final HttpHeaders headers;

		protected final HttpRequest httpRequest;

		@Context
		protected HttpRequest request;

		@Context
		protected UriInfo uriInfo;

		public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event,
				WechatIdentityProvider provider) {
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

				if (authorizationCode != null) {

					String appId = getConfig().getConfig().get("clientId");
					String appSecret = getConfig().getClientSecret();

					System.out.println(String.join(" ", "app id", appId, "query param client id", clientId));
					JsonNode resJson = null;
					if (clientId.equals("wechatbrowser") || clientId.equals("wechatsite")) {
						// 微信登录
						String ua = this.session.getContext().getRequestHeaders().getHeaderString("user-agent")
								.toLowerCase();

						appId = isWechatBrowser(ua) ? WECHAT_PUBLIC_ID : WECHAT_WEBSITE_ID;
						appSecret = isWechatBrowser(ua) ? WECHAT_PUBLIC_SECRET : WECHAT_WEBSITE_SECRET;
						// 获取访问token
						logger.info("微信请求地址： " + (UriBuilder.fromUri(TOKEN_URL)
								.queryParam(OAUTH2_PARAMETER_CLIENT_ID, appId)
								.queryParam(OAUTH2_PARAMETER_CLIENT_SECRET, appSecret)
								.queryParam("code", authorizationCode)
								.queryParam(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
								.queryParam(OAUTH2_PARAMETER_REDIRECT_URI,
										Urls.identityProviderAuthnResponse(context.getUri().getBaseUri(),
												providerConfig.getAlias(), context.getRealm().getName()).toString())
								.toString()));

						resJson = SimpleHttp.doGet(TOKEN_URL, session).param(OAUTH2_PARAMETER_CLIENT_ID, appId)
								.param(OAUTH2_PARAMETER_CLIENT_SECRET, appSecret).param("code", authorizationCode)
								.param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
								.param(OAUTH2_PARAMETER_REDIRECT_URI,
										Urls.identityProviderAuthnResponse(context.getUri().getBaseUri(),
												providerConfig.getAlias(), context.getRealm().getName()).toString())
								.asJson();

						// 注册用户，获取用户的个人信息
						String userInfoUrl = PROFILE_URL.replace("ACCESS_TOKEN", resJson.get("access_token").asText())
								.replace("OPENID", resJson.get("openid").asText());
						resJson = SimpleHttp.doGet(userInfoUrl, session).asJson();

					} else {

						// 小程序的登录
						logger.info("小程序请求地址： " + UriBuilder.fromUri("https://api.weixin.qq.com/sns/jscode2session")
								.queryParam(OAUTH2_PARAMETER_CLIENT_ID, appId)
								.queryParam(OAUTH2_PARAMETER_CLIENT_SECRET, appSecret)
								.queryParam("js_code", authorizationCode)
								.queryParam(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
								.queryParam(OAUTH2_PARAMETER_REDIRECT_URI,
										Urls.identityProviderAuthnResponse(context.getUri().getBaseUri(),
												providerConfig.getAlias(), context.getRealm().getName()).toString())
								.toString());
						resJson = SimpleHttp.doGet("https://api.weixin.qq.com/sns/jscode2session", session)
								.param(OAUTH2_PARAMETER_CLIENT_ID, appId)
								.param(OAUTH2_PARAMETER_CLIENT_SECRET, appSecret).param("js_code", authorizationCode)
								.param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
								.param(OAUTH2_PARAMETER_REDIRECT_URI,
										Urls.identityProviderAuthnResponse(context.getUri().getBaseUri(),
												providerConfig.getAlias(), context.getRealm().getName()).toString())
								.asJson();

						logger.info("返回结果： " + resJson);
						if (resJson.get("unionid") == null) {
//                    return this.callback.error(resJson.toString());
							return Response.status(400).entity(resJson.toString()).build();
						}
					}

					BrokeredIdentityContext federatedIdentity = doGetFederatedIdentity(resJson.toString());

					federatedIdentity.setIdpConfig(providerConfig);
					federatedIdentity.setIdp(provider);
					federatedIdentity.setAuthenticationSession(authSession);
					return callback.authenticated(federatedIdentity);
				}
			} catch (WebApplicationException e) {
				return e.getResponse();
			} catch (Exception e) {
				logger.error("Failed to make identity provider (weixin) oauth callback", e);
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
