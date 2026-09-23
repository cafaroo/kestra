package io.kestra.webserver.rooting;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import io.kestra.core.contexts.configuration.StudioConfiguration;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.web.router.DefaultRouter;
import io.micronaut.web.router.RouteBuilder;
import io.micronaut.web.router.UriRouteMatch;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;

@Singleton
@Requires(missingClasses = "io.kestra.ee.webserver.rooting.TenantAliasingRooterEE")
@Replaces(DefaultRouter.class)
public class TenantAliasingRooter extends DefaultRouter {

    protected static final List<Pattern> EXCLUDED_ROUTES = List.of(
        Pattern.compile("/api/v1/main/.*"),
        Pattern.compile("/api/v1/configs"),
        Pattern.compile("/api/v1/configs/login"),
        Pattern.compile("/api/v1/tenants.*")
    );

    private final StudioConfiguration studioConfiguration;

    @Inject
    public TenantAliasingRooter(Collection<RouteBuilder> builders, final StudioConfiguration studioConfiguration) {
        super(builders);
        this.studioConfiguration = studioConfiguration;
    }

    @SneakyThrows
    @Override
    public <T, R> UriRouteMatch<T, R> findClosest(HttpRequest<?> request) {
        String rawPath = request.getPath();
        UriRouteMatch<T, R> closest = super.findClosest(request);
        if (closest != null || bypassRooting()) {
            return closest;
        }

        boolean excluded = EXCLUDED_ROUTES.stream().anyMatch(route -> route.matcher(rawPath).matches());
        if (rawPath.startsWith("/api/v1/") && !excluded) {
            String rewrittenRawPath = rawPath.replaceFirst("^/api/v1", "/api/v1/" + getTenantId());
            URI updatedUri = new URI(rebuildRawUri(request, rewrittenRawPath));
            return super.findClosest(request.toMutableRequest().uri(updatedUri));
        }
        return null;
    }

    private static String rebuildRawUri(HttpRequest<?> request, String rawPath) {
        URI originalUri = request.getUri();
        StringBuilder builder = new StringBuilder();
        if (originalUri.getScheme() != null) {
            builder.append(originalUri.getScheme()).append(':');
        }
        builder.append("//");
        if (originalUri.getRawUserInfo() != null) {
            builder.append(originalUri.getRawUserInfo()).append('@');
        }
        builder.append(bracketIfIpv6(request.getServerAddress().getHostName()))
            .append(':').append(request.getServerAddress().getPort());
        builder.append(rawPath);
        if (originalUri.getRawQuery() != null) {
            builder.append('?').append(originalUri.getRawQuery());
        }
        if (originalUri.getRawFragment() != null) {
            builder.append('#').append(originalUri.getRawFragment());
        }
        return builder.toString();
    }

    private static String bracketIfIpv6(String host) {
        return host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
    }

    protected String getTenantId() {
        return "main";
    }

    protected boolean bypassRooting() {
        return studioConfiguration.enabled();
    }
}
