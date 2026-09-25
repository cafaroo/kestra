<template>
    <KsDropdown v-if="enabled && currentTenant" trigger="click" @command="switchTenant">
        <KsButton class="tenant-switcher" size="small">
            <span class="tenant-avatar">{{ initials }}</span>
            <span class="tenant-copy">
                <span class="tenant-name">{{ currentTenant.name }}</span>
                <span class="tenant-id">{{ currentTenant.id }}</span>
            </span>
            <ChevronDown class="tenant-chevron" :size="16" />
        </KsButton>
        <template #dropdown>
            <KsDropdownMenu>
                <KsDropdownItem
                    v-for="tenant in tenantStore.activeTenants"
                    :key="tenant.id"
                    :command="tenant.id"
                    :disabled="tenant.id === currentTenant.id"
                >
                    <span class="tenant-item-avatar">{{ tenantInitials(tenant.name) }}</span>
                    <span class="tenant-item-copy">
                        <span>{{ tenant.name }}</span>
                        <span class="tenant-item-id">{{ tenant.id }}</span>
                    </span>
                    <Check v-if="tenant.id === currentTenant.id" :size="16" />
                </KsDropdownItem>
                <KsDropdownItem command="__manage_tenants__" divided>
                    {{ $t("tenant.names") }}
                </KsDropdownItem>
            </KsDropdownMenu>
        </template>
    </KsDropdown>
</template>

<script setup lang="ts">
    import {computed, onMounted} from "vue"
    import {useRoute, useRouter} from "vue-router"
    import {KsButton, KsDropdown, KsDropdownItem, KsDropdownMenu} from "@kestra-io/design-system"
    import ChevronDown from "vue-material-design-icons/ChevronDown.vue"
    import Check from "vue-material-design-icons/Check.vue"

    import {useMiscStore} from "override/stores/misc"
    import {useTenantsStore} from "../../stores/tenants"
    import {setActiveTenant} from "override/utils/route"

    const route = useRoute()
    const router = useRouter()
    const miscStore = useMiscStore()
    const tenantStore = useTenantsStore()

    const enabled = computed(() => Boolean((miscStore.configs as {isStudioEnabled?: boolean} | undefined)?.isStudioEnabled))
    const routeTenant = computed(() => {
        const value = route.params.tenant
        return Array.isArray(value) ? value[0] : value
    })
    const currentTenant = computed(() => tenantStore.find(routeTenant.value))

    const tenantInitials = (name: string): string => name
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0]?.toUpperCase())
        .join("")

    const initials = computed(() => currentTenant.value ? tenantInitials(currentTenant.value.name) : "")

    onMounted(async () => {
        if (enabled.value) await tenantStore.load()
    })

    function safeTarget(tenantId: string) {
        const name = typeof route.name === "string" ? route.name : ""
        const params = {...route.params, tenant: tenantId}

        if (["home", "flows/list", "namespaces/list", "plugins/list", "ai"].includes(name)) {
            return {name, params, query: route.query}
        }
        if (name.startsWith("flows/")) return {name: "flows/list", params: {tenant: tenantId}}
        if (name.startsWith("namespaces/")) return {name: "namespaces/list", params: {tenant: tenantId}}
        if (name.startsWith("blueprints")) {
            return {name: "blueprints", params: {tenant: tenantId, kind: "flow", tab: "community"}}
        }
        if (name === "studio/overview" || name === "studio/infrastructure" || name === "studio/releases") {
            return {name, params: {tenant: tenantId}, query: route.query}
        }
        return {name: "home", params: {tenant: tenantId}}
    }

    async function switchTenant(command: string | number | object): Promise<void> {
        if (typeof command !== "string") return
        if (command === "__manage_tenants__") {
            await router.push({name: "studio/tenants"})
            return
        }
        if (command === routeTenant.value) return
        tenantStore.remember(command)
        setActiveTenant(command)
        await router.push(safeTarget(command))
    }
</script>

<style scoped lang="scss">
    .tenant-switcher {
        width: 100%;
        height: auto;
        justify-content: flex-start;
        padding: var(--ks-spacing-2);
        border-color: var(--ks-border-default);
        background: var(--ks-bg-elevated);
    }

    .tenant-avatar,
    .tenant-item-avatar {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        flex: 0 0 auto;
        width: var(--ks-spacing-6);
        height: var(--ks-spacing-6);
        border-radius: var(--ks-radius-lg);
        background: var(--ks-bg-tag-active);
        color: var(--ks-text-link);
        font-size: var(--ks-font-size-xs);
        font-weight: var(--ks-font-weight-bold);
    }

    .tenant-copy,
    .tenant-item-copy {
        display: flex;
        flex: 1 1 auto;
        min-width: 0;
        flex-direction: column;
        align-items: flex-start;
    }

    .tenant-name {
        max-width: 100%;
        overflow: hidden;
        color: var(--ks-text-primary);
        font-size: var(--ks-font-size-xs);
        font-weight: var(--ks-font-weight-semibold);
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .tenant-id,
    .tenant-item-id {
        color: var(--ks-text-secondary);
        font-family: var(--ks-font-family-mono);
        font-size: var(--ks-font-size-2xs);
    }

    .tenant-chevron {
        flex: 0 0 auto;
        color: var(--ks-icon-muted);
    }

    .tenant-item-copy {
        min-width: 10rem;
    }
</style>
