package io.kestra.core.studio;

import java.time.Instant;

import io.kestra.core.models.HasUID;
import io.kestra.core.models.TenantInterface;

import io.micronaut.core.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class ReleaseTarget implements HasUID, TenantInterface {
    @NotBlank
    private String tenantId;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9][a-z0-9_-]*$")
    private String id;

    @NotBlank
    private String name;

    @Nullable
    private String runtimeUrl;

    @Nullable
    private String desiredCommit;

    @Nullable
    private String deployedCommit;

    @NotNull
    private RuntimeHealth health;

    @NotNull
    private ReleaseTargetStatus status;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    @Override
    public String uid() {
        return tenantId + "|" + id;
    }
}
