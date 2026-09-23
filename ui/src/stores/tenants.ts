import {computed, ref} from "vue"
import {defineStore} from "pinia"
import {useClient} from "@kestra-io/kestra-sdk"
import {apiUrlWithoutTenants} from "override/utils/route"

export interface StudioTenant {
    id: string
    name: string
    status: "ACTIVE" | "DISABLED"
    createdAt: string
    updatedAt: string
}

const ACTIVE_TENANT_STORAGE_KEY = "kestra-active-tenant"

export const useTenantsStore = defineStore("tenants", () => {
    const tenants = ref<StudioTenant[]>([])
    const loading = ref(false)
    const loaded = ref(false)
    const client = useClient()

    const activeTenants = computed(() => tenants.value.filter((tenant) => tenant.status === "ACTIVE"))

    async function load(force = false): Promise<StudioTenant[]> {
        if (loaded.value && !force) return tenants.value

        loading.value = true
        try {
            const response = await client.get<StudioTenant[]>(`${apiUrlWithoutTenants()}/tenants`)
            tenants.value = response.data
            loaded.value = true
            return tenants.value
        } finally {
            loading.value = false
        }
    }

    function find(id: string | undefined): StudioTenant | undefined {
        return id ? tenants.value.find((tenant) => tenant.id === id) : undefined
    }

    function preferredTenantId(): string | undefined {
        let stored: string | null = null
        try {
            stored = localStorage.getItem(ACTIVE_TENANT_STORAGE_KEY)
        } catch {
            // localStorage may be unavailable in hardened browser contexts.
        }

        if (stored && activeTenants.value.some((tenant) => tenant.id === stored)) return stored
        return activeTenants.value[0]?.id
    }

    function remember(id: string): void {
        try {
            localStorage.setItem(ACTIVE_TENANT_STORAGE_KEY, id)
        } catch {
            // Best-effort only; routing remains authoritative.
        }
    }

    return {
        tenants,
        activeTenants,
        loading,
        loaded,
        load,
        find,
        preferredTenantId,
        remember,
    }
})
