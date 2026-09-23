package io.kestra.core.studio;

import java.time.Instant;

import io.kestra.core.models.HasUID;
import io.kestra.core.models.TenantInterface;

import io.micronaut.core.annotation.Nullable;
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
public class ReleaseRecord implements HasUID, TenantInterface {
    @NotBlank
    private String tenantId;

    @NotBlank
    private String id;

    @NotBlank
    private String targetId;

    @NotBlank
    private String sourceCommit;

    @Nullable
    private String previousCommit;

    @NotNull
    private ReleaseStatus status;

    @Nullable
    private String actor;

    @Nullable
    private String error;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    @Override
    public String uid() {
        return tenantId + "|" + id;
    }
}
