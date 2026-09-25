package io.kestra.webserver.filter;

import java.util.List;
import java.util.regex.Pattern;

import io.kestra.core.contexts.configuration.StudioConfiguration;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Inject;

@ServerFilter("/api/v1/**")
public class StudioDefinitionOnlyFilter {
    private static final List<Pattern> BLOCKED = List.of(
        Pattern.compile("^/api/v1/[^/]+/executions(?:/.*)?$"),
        Pattern.compile("^/api/v1/[^/]+/logs(?:/.*)?$"),
        Pattern.compile("^/api/v1/[^/]+/metrics(?:/.*)?$"),
        Pattern.compile("^/api/v1/[^/]+/outputs(?:/.*)?$"),
        Pattern.compile("^/api/v1/[^/]+/triggers(?:/.*)?$"),
        Pattern.compile("^/api/v1/[^/]+/kv(?:/.*)?$"),
        Pattern.compile("^/api/v1/[^/]+/namespaces/[^/]+/kv(?:/.*)?$")
    );

    private final StudioConfiguration configuration;

    @Inject
    public StudioDefinitionOnlyFilter(final StudioConfiguration configuration) {
        this.configuration = configuration;
    }

    @RequestFilter
    public void filterRequest(final HttpRequest<?> request) {
        if (!configuration.enabled() || !configuration.definitionOnly()) {
            return;
        }

        String path = request.getPath();
        if (BLOCKED.stream().anyMatch(pattern -> pattern.matcher(path).matches())) {
            throw new HttpStatusException(
                HttpStatus.FORBIDDEN,
                "Runtime data APIs are unavailable in definition-only Central Studio"
            );
        }
    }
}
