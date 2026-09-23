import {ref} from "vue"
import {defineStore} from "pinia"
import {useClient} from "@kestra-io/kestra-sdk"
import {apiUrl} from "override/utils/route"

export type RuntimeHealth = "UNKNOWN" | "HEALTHY" | "WARNING" | "FAILED"
export type ReleaseTargetStatus = "ACTIVE" | "DISABLED"
export type ReleaseStatus = "VALIDATING" | "READY" | "APPLYING" | "APPLIED" | "FAILED"

export interface ReleaseTarget {
    tenantId: string
    id: string
    name: string
    runtimeUrl?: string | null
    desiredCommit?: string | null
    deployedCommit?: string | null
    health: RuntimeHealth
    status: ReleaseTargetStatus
    createdAt: string
    updatedAt: string
}

export interface InfrastructureBoundary {
    id: string
    kind: "STUDIO" | "RUNTIME" | "EXTERNAL"
    title: string
    subtitle?: string | null
}

export interface InfrastructurePort {
    id: string
    kind: "HTTP" | "DATABASE" | "NETWORK" | "RELEASE" | "EXTERNAL"
    label?: string | null
}

export interface InfrastructureNode {
    id: string
    kind: "RUNTIME" | "SERVICE" | "DATABASE" | "REPOSITORY" | "CONTROL" | "EXTERNAL" | "NETWORK"
    name: string
    groupId: string
    status?: string | null
    image?: string | null
    version?: string | null
    ports: InfrastructurePort[]
    metadata: Record<string, string>
}

export interface InfrastructureEdge {
    id: string
    source: string
    sourcePort?: string | null
    target: string
    targetPort?: string | null
    kind: "DEFINITION" | "CONTROL" | "NETWORK" | "DEPENDENCY" | "EXTERNAL"
    label?: string | null
}

export interface InfrastructureDefinition {
    tenantId: string
    targetId: string
    revision: number
    boundaries: InfrastructureBoundary[]
    nodes: InfrastructureNode[]
    edges: InfrastructureEdge[]
    sourceCommit?: string | null
    updatedAt: string
}

export interface ReleaseRecord {
    tenantId: string
    id: string
    targetId: string
    sourceCommit: string
    previousCommit?: string | null
    status: ReleaseStatus
    actor?: string | null
    error?: string | null
    createdAt: string
    updatedAt: string
}

export const useStudioControlPlaneStore = defineStore("studio-control-plane", () => {
    const client = useClient()
    const targets = ref<ReleaseTarget[]>([])
    const infrastructure = ref<InfrastructureDefinition | null>(null)
    const releases = ref<ReleaseRecord[]>([])
    const loading = ref(false)

    async function loadTargets(): Promise<ReleaseTarget[]> {
        loading.value = true
        try {
            const {data} = await client.get<ReleaseTarget[]>(`${apiUrl()}/studio/release-targets`)
            targets.value = data
            return data
        } finally {
            loading.value = false
        }
    }

    async function saveTarget(id: string, name: string, runtimeUrl?: string): Promise<ReleaseTarget> {
        const {data} = await client.put<ReleaseTarget>(
            `${apiUrl()}/studio/release-targets/${id}`,
            {name, runtimeUrl},
        )
        await loadTargets()
        return data
    }

    async function loadInfrastructure(targetId: string): Promise<InfrastructureDefinition | null> {
        try {
            const {data} = await client.get<InfrastructureDefinition>(
                `${apiUrl()}/studio/infrastructure/${targetId}`,
                {showMessageOnError: false},
            )
            infrastructure.value = data
            return data
        } catch (error) {
            const status = (error as {status?: number; response?: {status?: number}})?.response?.status
                ?? (error as {status?: number})?.status
            if (status === 404) {
                infrastructure.value = null
                return null
            }
            throw error
        }
    }

    async function saveInfrastructure(
        targetId: string,
        definition: Pick<InfrastructureDefinition, "boundaries" | "nodes" | "edges" | "sourceCommit">,
    ): Promise<InfrastructureDefinition> {
        const {data} = await client.put<InfrastructureDefinition>(
            `${apiUrl()}/studio/infrastructure/${targetId}`,
            definition,
        )
        infrastructure.value = data
        return data
    }

    async function loadReleases(targetId: string): Promise<ReleaseRecord[]> {
        const {data} = await client.get<ReleaseRecord[]>(
            `${apiUrl()}/studio/release-targets/${targetId}/releases`,
        )
        releases.value = data
        return data
    }

    async function prepareRelease(targetId: string, sourceCommit: string): Promise<ReleaseRecord> {
        const {data} = await client.post<ReleaseRecord>(
            `${apiUrl()}/studio/release-targets/${targetId}/releases`,
            {sourceCommit},
        )
        await Promise.all([loadTargets(), loadReleases(targetId)])
        return data
    }

    return {
        targets,
        infrastructure,
        releases,
        loading,
        loadTargets,
        saveTarget,
        loadInfrastructure,
        saveInfrastructure,
        loadReleases,
        prepareRelease,
    }
})
