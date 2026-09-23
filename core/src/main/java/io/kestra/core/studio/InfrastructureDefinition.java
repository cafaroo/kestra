package io.kestra.core.studio;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
public class InfrastructureDefinition implements HasUID, TenantInterface {
    @NotBlank
    private String tenantId;

    @NotBlank
    private String targetId;

    private int revision;

    @Builder.Default
    private List<Boundary> boundaries = List.of();

    @Builder.Default
    private List<Node> nodes = List.of();

    @Builder.Default
    private List<Edge> edges = List.of();

    @Nullable
    private String sourceCommit;

    @NotNull
    private Instant updatedAt;

    @Override
    public String uid() {
        return tenantId + "|" + targetId;
    }

    public record Boundary(String id, Kind kind, String title, @Nullable String subtitle) {
        public enum Kind {
            STUDIO,
            RUNTIME,
            EXTERNAL
        }
    }

    public record Node(
        String id,
        Kind kind,
        String name,
        String groupId,
        @Nullable String status,
        @Nullable String image,
        @Nullable String version,
        List<Port> ports,
        Map<String, String> metadata) {
        public enum Kind {
            RUNTIME,
            SERVICE,
            DATABASE,
            REPOSITORY,
            CONTROL,
            EXTERNAL,
            NETWORK
        }

        public Node {
            ports = ports == null ? List.of() : List.copyOf(ports);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    public record Port(String id, Kind kind, @Nullable String label) {
        public enum Kind {
            HTTP,
            DATABASE,
            NETWORK,
            RELEASE,
            EXTERNAL
        }
    }

    public record Edge(
        String id,
        String source,
        @Nullable String sourcePort,
        String target,
        @Nullable String targetPort,
        Kind kind,
        @Nullable String label) {
        public enum Kind {
            DEFINITION,
            CONTROL,
            NETWORK,
            DEPENDENCY,
            EXTERNAL
        }
    }
}
