package io.kestra.webserver.tenants;

import io.kestra.core.contexts.configuration.StudioConfiguration;
import io.kestra.core.models.tenants.TenantStatus;
import io.kestra.core.tenant.TenantService;
import io.kestra.core.validations.validator.TenantIdValidator;

import io.micronaut.core.order.Ordered;
import io.micronaut.http.BasicHttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.uri.UriMatchInfo;
import jakarta.inject.Inject;

import static io.kestra.core.tenant.TenantService.MAIN_TENANT;
import static io.kestra.core.tenant.TenantService.TENANT_PATH_ATTRIBUTE;

@ServerFilter("/**")
public class TenantValidationFilter implements Ordered {
    private final StudioConfiguration studioConfiguration;
    private final TenantService tenantService;

    @Inject
    public TenantValidationFilter(final StudioConfiguration studioConfiguration, final TenantService tenantService) {
        this.studioConfiguration = studioConfiguration;
        this.tenantService = tenantService;
    }

    @RequestFilter
    public void filterRequest(HttpRequest<?> request) {
        UriMatchInfo routeMatch = BasicHttpAttributes.getRouteMatchInfo(request).orElse(null);
        if (routeMatch == null || !routeMatch.getVariableValues().containsKey(TENANT_PATH_ATTRIBUTE)) {
            return;
        }

        String tenant = (String) routeMatch.getVariableValues().get(TENANT_PATH_ATTRIBUTE);
        if (!studioConfiguration.enabled()) {
            if (tenant != null && !MAIN_TENANT.equals(tenant)) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Tenant must be 'main' for OSS version");
            }
            return;
        }

        if (!TenantIdValidator.isValid(tenant)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid tenant id: '" + tenant + "'");
        }

        var resolved = tenantService.findTenant(tenant)
            .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Tenant not found: '" + tenant + "'"));

        if (resolved.getStatus() != TenantStatus.ACTIVE) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Tenant is disabled: '" + tenant + "'");
        }
    }
}
