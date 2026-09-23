export type PromotionStatus =
    | "IN_SYNC"
    | "OUT_OF_SYNC"
    | "NOT_PROMOTED"
    | "DEPLOYING"
    | "FAILED"
    | "UNREACHABLE"

export type PromotionCompatibilityStatus =
    | "COMPATIBLE"
    | "INCOMPATIBLE"
    | "UNKNOWN"

export type PromotionArtefactKind = "AUTOMATION" | "RUNTIME"

export type PromotionTransportType = "FORGEJO_RELEASE"

export interface PromotionTarget {
    id: string
    displayName: string
    environment: string
    transport: PromotionTransportType
    runtimeId: string
    confirmationGate: boolean
    enabled: boolean
    automationStatus: PromotionStatus
    runtimeStatus: PromotionStatus
    health: "HEALTHY" | "DEGRADED" | "UNKNOWN"
}

export interface PromotionCandidateSummary {
    artefactKind: PromotionArtefactKind
    sourceRevision: string
    targetId: string
    sourceDigest?: string
    targetDeployedDigest?: string
    compatibility: PromotionCompatibilityStatus
    confirmationRequired: boolean
}
