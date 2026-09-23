package io.kestra.core.models.tenants;

import java.time.Instant;

import io.kestra.core.models.HasUID;
import io.kestra.core.validations.TenantId;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Tenant implements HasUID {
    @NotNull
    @TenantId
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private TenantStatus status;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    @Override
    public String uid() {
        return id;
    }
}
