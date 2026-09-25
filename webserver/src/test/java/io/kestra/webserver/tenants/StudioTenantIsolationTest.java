package io.kestra.webserver.tenants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.tenant.TenantService;
import io.kestra.core.utils.IdUtils;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@KestraTest
@Property(name = "kestra.server-type", value = "WEBSERVER")
@Property(name = "millblad.studio.enabled", value = "true")
@Property(name = "millblad.studio.definition-only", value = "true")
class StudioTenantIsolationTest {
    @Inject
    TenantService tenantService;

    @Inject
    @Client("/")
    ReactorHttpClient client;

    private String tenant;

    @BeforeEach
    void createTenant() {
        tenant = "tenant-" + IdUtils.create().toLowerCase();
        tenantService.createTenant(tenant, "Test tenant");
    }

    @Test
    void shouldAllowDefinitionApiForActiveTenant() {
        var response = client.toBlocking().exchange(
            HttpRequest.GET("/api/v1/" + tenant + "/flows/search"),
            String.class
        );

        assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
    }

    @Test
    void shouldRejectUnknownTenant() {
        HttpClientResponseException exception = catchThrowableOfType(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/unknown-" + IdUtils.create().toLowerCase() + "/flows/search"),
                String.class
            )
        );

        assertThat(exception.code()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void shouldRejectDisabledTenant() {
        tenantService.disableTenant(tenant);

        HttpClientResponseException exception = catchThrowableOfType(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/" + tenant + "/flows/search"),
                String.class
            )
        );

        assertThat(exception.code()).isEqualTo(HttpStatus.FORBIDDEN.getCode());
    }

    @Test
    void shouldNotAliasTenantlessApiToMainInStudio() {
        HttpClientResponseException exception = catchThrowableOfType(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/flows/search"),
                String.class
            )
        );

        assertThat(exception.code()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void shouldBlockRuntimeDataApiInDefinitionOnlyStudio() {
        HttpClientResponseException exception = catchThrowableOfType(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/" + tenant + "/executions?page=1&size=10"),
                String.class
            )
        );

        assertThat(exception.code()).isEqualTo(HttpStatus.FORBIDDEN.getCode());
    }
}
