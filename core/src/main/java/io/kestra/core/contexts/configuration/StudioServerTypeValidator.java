package io.kestra.core.contexts.configuration;

import java.util.Set;

import io.kestra.core.models.ServerType;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Context
@Requires(property = "millblad.studio.enabled", value = "true")
@Requires(property = "millblad.studio.definition-only", value = "true")
@Requires(property = "kestra.server-type")
public class StudioServerTypeValidator {
    private static final Set<ServerType> ALLOWED = Set.of(ServerType.WEBSERVER, ServerType.INDEXER);

    private final ServerType serverType;

    @Inject
    public StudioServerTypeValidator(@Value("${kestra.server-type}") final ServerType serverType) {
        this.serverType = serverType;
    }

    @PostConstruct
    void validate() {
        if (!ALLOWED.contains(serverType)) {
            throw new IllegalStateException(
                "Definition-only Central Studio may only run WEBSERVER or INDEXER roles; got " + serverType
            );
        }
    }
}
