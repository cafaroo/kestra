package io.kestra.webserver.millblad.promote;

import java.time.Instant;

public final class PromotionModels {
    private PromotionModels() {
    }

    public enum PromotionStatus {
        IN_SYNC,
        OUT_OF_SYNC,
        NOT_PROMOTED,
        DEPLOYING,
        FAILED,
        UNREACHABLE
    }

    public enum CompatibilityStatus {
        COMPATIBLE,
        INCOMPATIBLE,
        UNKNOWN
    }

    public enum ArtefactKind {
        AUTOMATION,
        RUNTIME
    }

    public enum TransportType {
        FORGEJO_RELEASE
    }

    public enum RuntimeHealth {
        HEALTHY,
        DEGRADED,
        UNKNOWN
    }

    public enum CandidateStatus {
        CREATED,
        VALIDATING,
        READY,
        CONFIRMATION_REQUIRED,
        PUBLISHING,
        RELEASED,
        DEPLOYMENT_REQUESTED,
        DEPLOYING,
        DEPLOYED,
        FAILED
    }

    public record PromotionTarget(
        String id,
        String displayName,
        String environment,
        TransportType transport,
        String runtimeId,
        boolean confirmationGate,
        boolean enabled,
        PromotionStatus automationStatus,
        PromotionStatus runtimeStatus,
        RuntimeHealth health
    ) {
    }

    public record CreatePromotionCandidateRequest(
        ArtefactKind artefactKind,
        String artefactId,
        String sourceRevision,
        String targetId
    ) {
    }

    public record PromotionCandidate(
        String id,
        ArtefactKind artefactKind,
        String artefactId,
        String sourceRevision,
        String targetId,
        CompatibilityStatus compatibility,
        CandidateStatus status,
        boolean confirmationRequired,
        String sourceDigest,
        String targetDeployedDigest,
        Instant createdAt
    ) {
    }
}
