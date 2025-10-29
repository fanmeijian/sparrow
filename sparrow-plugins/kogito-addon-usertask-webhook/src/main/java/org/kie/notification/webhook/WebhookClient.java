package org.kie.notification.webhook;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient
public interface WebhookClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Void> send(@HeaderParam("webhookUrl") String webhookUrl,
                   @HeaderParam("Authorization") String token,
                   WebhookPayload payload);
}

record WebhookPayload(String taskId, String taskName, String owner, String deadline) {}