package cn.sparrowmini.identity.keycloak;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.spi.ResteasyClientClassicProvider;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/** Fixed-realm adapter around the official Admin Client. */
public final class KeycloakAdminClient implements AutoCloseable {
    public static final class UpstreamException extends IllegalStateException {
        private UpstreamException(String detail) {
            super("Keycloak management request failed (" + detail + ")");
        }
    }

    private final Keycloak client;
    private final String realm;

    public KeycloakAdminClient(URI base, String realm, String clientId, String secret) {
        if (!Set.of("http", "https").contains(base.getScheme()) || base.getHost() == null
                || base.getUserInfo() != null || base.getQuery() != null || base.getFragment() != null) {
            throw new IllegalArgumentException("Invalid Keycloak base URI");
        }
        this.realm = required(realm);
        client = KeycloakBuilder.builder().serverUrl(base.toString()).realm(this.realm)
                .grantType("client_credentials").clientId(required(clientId)).clientSecret(required(secret))
                .resteasyClient(ResteasyClientClassicProvider.createClientBuilder()
                        .connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS)
                        .build().register(org.keycloak.admin.client.JacksonProvider.class, 100))
                .build();
    }

    private static String required(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Required Keycloak configuration missing");
        return value;
    }

    public <T> T read(Function<RealmResource, T> action) {
        try {
            return action.apply(client.realm(realm));
        } catch (WebApplicationException | ProcessingException error) {
            for (Throwable cause = error; cause != null; cause = cause.getCause()) {
                if (cause instanceof WebApplicationException responseError && responseError.getResponse() != null) {
                    try (var response = responseError.getResponse()) {
                        throw new UpstreamException("HTTP " + response.getStatus());
                    }
                }
            }
            throw new UpstreamException(error.getClass().getSimpleName());
        }
    }

    public void write(Consumer<RealmResource> action) {
        read(realm -> { action.accept(realm); return null; });
    }

    public static void requireSuccess(Response response) {
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            throw new UpstreamException("HTTP " + response.getStatus());
        }
    }

    @Override public void close() {
        client.close();
    }
}
