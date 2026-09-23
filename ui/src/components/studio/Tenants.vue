<template>
    <TopNavBar :title="$t('tenant.names')" hideMainIcon>
        <template #actions>
            <KsButton type="primary" @click="createOpen = true">
                {{ $t("create") }} {{ $t("tenant.name") }}
            </KsButton>
        </template>
    </TopNavBar>

    <KsListingPage class="tenant-page">
        <div class="tenant-grid">
            <KsCard v-for="tenant in tenantStore.tenants" :key="tenant.id" shadow="never">
                <template #header>
                    <div class="tenant-heading">
                        <div>
                            <KsText tag="h3" size="large">{{ tenant.name }}</KsText>
                            <KsText size="small" class="tenant-id">{{ tenant.id }}</KsText>
                        </div>
                        <KsTag
                            :type="tenant.status === 'ACTIVE' ? 'success' : 'info'"
                            size="small"
                        >
                            {{ tenant.status === "ACTIVE" ? $t("active") : $t("disabled") }}
                        </KsTag>
                    </div>
                </template>

                <template #footer>
                    <div class="tenant-actions">
                        <KsButton
                            v-if="tenant.status === 'ACTIVE'"
                            size="small"
                            @click="openTenant(tenant.id)"
                        >
                            {{ $t("open") }}
                        </KsButton>
                        <KsButton
                            v-if="tenant.status === 'ACTIVE'"
                            size="small"
                            @click="disableTenant(tenant.id)"
                        >
                            {{ $t("disable") }}
                        </KsButton>
                    </div>
                </template>
            </KsCard>
        </div>
    </KsListingPage>

    <KsDialog
        v-model="createOpen"
        :title="`${$t('create')} ${$t('tenant.name')}`"
        :dirty="Boolean(id || name)"
        destroyOnClose
    >
        <KsForm>
            <KsFormItem :label="$t('id')">
                <KsInput v-model="id" />
            </KsFormItem>
            <KsFormItem :label="$t('name')">
                <KsInput v-model="name" />
            </KsFormItem>
        </KsForm>

        <template #footer>
            <KsButton @click="closeCreate">{{ $t("cancel") }}</KsButton>
            <KsButton type="primary" :disabled="!valid" @click="createTenant">
                {{ $t("create") }}
            </KsButton>
        </template>
    </KsDialog>
</template>

<script setup lang="ts">
    import {computed, onMounted, ref} from "vue"
    import {useRouter} from "vue-router"
    import {
        KsButton,
        KsCard,
        KsDialog,
        KsForm,
        KsFormItem,
        KsInput,
        KsListingPage,
        KsTag,
        KsText,
    } from "@kestra-io/design-system"

    import TopNavBar from "../layout/TopNavBar.vue"
    import {useTenantsStore} from "../../stores/tenants"
    import {setActiveTenant} from "override/utils/route"

    const router = useRouter()
    const tenantStore = useTenantsStore()

    const createOpen = ref(false)
    const id = ref("")
    const name = ref("")

    const valid = computed(() =>
        /^[a-z0-9][a-z0-9_-]{0,99}$/.test(id.value) && name.value.trim().length > 0,
    )

    onMounted(() => tenantStore.load())

    function closeCreate() {
        createOpen.value = false
        id.value = ""
        name.value = ""
    }

    async function createTenant() {
        if (!valid.value) return
        const tenant = await tenantStore.create(id.value, name.value)
        closeCreate()
        openTenant(tenant.id)
    }

    async function disableTenant(tenantId: string) {
        await tenantStore.disable(tenantId)
    }

    function openTenant(tenantId: string) {
        tenantStore.remember(tenantId)
        setActiveTenant(tenantId)
        router.push({name: "studio/overview", params: {tenant: tenantId}})
    }
</script>

<style scoped lang="scss">
    .tenant-page {
        padding-inline: var(--ks-spacing-5);
    }

    .tenant-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(18rem, 1fr));
        gap: var(--ks-spacing-4);
    }

    .tenant-heading {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: var(--ks-spacing-3);
    }

    .tenant-id {
        font-family: var(--ks-font-family-mono);
        color: var(--ks-text-secondary);
    }

    .tenant-actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--ks-spacing-2);
    }
</style>
