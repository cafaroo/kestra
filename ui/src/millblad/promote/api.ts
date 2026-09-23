import {useClient} from "@kestra-io/kestra-sdk"
import {useRoute} from "vue-router"
import {apiUrlWithTenant} from "override/utils/route"
import type {
    PromotionCandidateSummary,
    PromotionTarget,
} from "./model"

export interface CreatePromotionCandidateRequest {
    artefactKind: "AUTOMATION" | "RUNTIME"
    artefactId: string
    sourceRevision: string
    targetId: string
}

export interface PromotionCandidate extends PromotionCandidateSummary {
    id: string
    artefactId: string
    status:
        | "CREATED"
        | "VALIDATING"
        | "READY"
        | "CONFIRMATION_REQUIRED"
        | "PUBLISHING"
        | "RELEASED"
        | "DEPLOYMENT_REQUESTED"
        | "DEPLOYING"
        | "DEPLOYED"
        | "FAILED"
    createdAt: string
}

export function usePromoteApi() {
    const client = useClient()
    const route = useRoute()
    const base = () => `${apiUrlWithTenant(route)}/promote`

    async function listTargets(): Promise<PromotionTarget[]> {
        const {data} = await client.get<PromotionTarget[]>(`${base()}/targets`)
        return data
    }

    async function createCandidate(request: CreatePromotionCandidateRequest): Promise<PromotionCandidate> {
        const {data} = await client.post<PromotionCandidate>(`${base()}/candidates`, request)
        return data
    }

    async function getCandidate(candidateId: string): Promise<PromotionCandidate> {
        const {data} = await client.get<PromotionCandidate>(`${base()}/candidates/${candidateId}`)
        return data
    }

    return {
        listTargets,
        createCandidate,
        getCandidate,
    }
}