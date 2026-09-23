<template>
    <TopNavBar :title="$t('overview')" hideMainIcon />

    <KsListingPage class="studio-page">
        <div class="target-grid">
            <KsCard v-for="target in store.targets" :key="target.id" shadow="never">
                <template #header>
                    <div class="card-heading">
                        <div>
                            <KsText tag="h3" size="large">{{ target.name }}</KsText>
                            <KsText size="small" class="mono">{{ target.id }}</KsText>
                        </div>
                        <KsTag :type="healthType(target.health)" size="small">{{ target.health }}</KsTag>
                    </div>
                </template>

                <div class="commit-grid">
                    <div>
                        <KsText size="small" class="label">{{ $t("source") }}</KsText>
                        <KsText class="mono">{{ target.desiredCommit || "—" }}</KsText>
                    </div>
                    <div>
                        <KsText size="small" class="label">{{ $t("revision") }}</KsText>
                        <KsText class="mono">{{ target.deployedCommit || "—" }}</KsText>
                    </div>
                </div>

                <template #footer>
                    <KsButton
                        v-if="target.runtimeUrl"
                        size="small"
                        tag="a"
                        target="_blank"
                        :href="target.runtimeUrl"
                    >
                        {{ $t("open") }}
                    </KsButton>
                </template>
            </KsCard>
        </div>
    </KsListingPage>
</template>

<script setup lang="ts">
    import {onMounted} from "vue"
    import {KsButton, KsCard, KsListingPage, KsTag, KsText} from "@kestra-io/design-system"
    import TopNavBar from "../layout/TopNavBar.vue"
    import {useStudioControlPlaneStore, type RuntimeHealth} from "../../stores/studioControlPlane"

    const store = useStudioControlPlaneStore()

    onMounted(() => store.loadTargets())

    function healthType(health: RuntimeHealth): "success" | "warning" | "danger" | "info" {
        if (health === "HEALTHY") return "success"
        if (health === "WARNING") return "warning"
        if (health === "FAILED") return "danger"
        return "info"
    }
</script>

<style scoped lang="scss">
    .studio-page {
        padding-inline: var(--ks-spacing-5);
    }

    .target-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(20rem, 1fr));
        gap: var(--ks-spacing-4);
    }

    .card-heading {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: var(--ks-spacing-3);
    }

    .commit-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: var(--ks-spacing-4);
    }

    .label {
        display: block;
        color: var(--ks-text-secondary);
    }

    .mono {
        font-family: var(--ks-font-family-mono);
    }
</style>
