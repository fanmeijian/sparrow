package cn.sparrowmini.keycloak.tokenpocket;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

/**
 * @author Sword
 */
public class TokenPocketIdentityProviderFactory extends AbstractIdentityProviderFactory<TokenPocketIdentityProvider>
        implements SocialIdentityProviderFactory<TokenPocketIdentityProvider> {

    public static final String PROVIDER_ID = "tokenpocket";

    public String getName() {
        return "TokenPocket";
    }

    public TokenPocketIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new TokenPocketIdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    @Override
    public IdentityProviderModel createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    public String getId() {
        return PROVIDER_ID;
    }
}
