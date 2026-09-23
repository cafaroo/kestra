<template>
    <TopNavBar :title="$t('promote.label')" hideMainIcon>
        <template #actions>
            <KsSelect v-model="targetId" size="small" @change="loadTarget">
                <KsOption
                    v-for="target in store.targets"
                    :key="target.id"
                    :label="target.name"
                    :value="target.id"
                />
            </KsSelect>
            <KsButton type="primary" :disabled="!targetId" @click="releaseOpen = true">
                {{ $t("promote.label") }}
            </KsButton>
        </template>
    </TopNavBar>

    <KsListingPage class="studio-page">
        <KsCard v-if="target" shadow="never">
            <template #header>
                <div class="heading">
                    <KsText tag="h3" size="large">{{ target.name }}</KsText>
                    <KsTag :type="target.health === 'HEALTHY' ? 'success' : 'info'" size="small">
                        {{ target.health }}
                    </KsTag>
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

        <KsCard v-if="store.releases.length" shadow="never" class="history">
            <template #header>{{ $t("revisions") }}</template>
            <div v-for="release in store.releases" :key="release.id" class="release-row">
                <span class="mono">{{ release.sourceCommit }}</span>
                <KsTag size="xs">{{ release.status }}</KsTag>
                <KsDateAgo :date="release.createdAt" />
            </div>
        </KsCard>
    </KsListingPage>

    <KsDialog v-model="releaseOpen" :title="$t('promote.label')" :dirty="Boolean(sourceCommit)">
        <KsForm>
            <KsFormItem :label="$t('commit_id')">
                <KsInput v-model="sourceCommit" />
            </KsFormItem>
        </KsForm>
        <template #footer>
            <KsButton @click="releaseOpen = false">{{ $t("cancel") }}</KsButton>
            <KsButton type="primary" :disabled="!sourceCommit.trim()" @click="prepare">
                {{ $t("promote.label") }}
            </KsButton>
        </template>
    </KsDialog>
</template>

<script setup lang="ts">
    import {computed, onMounted, ref} from "vue"
    import {
        KsButton,
        KsCard,
        KsDateAgo,
        KsDialog,
        KsForm,
        KsFormItem,
        KsInput,
        KsListingPage,
        KsOption,
        KsSelect,
        KsTag,
        KsText,
    } from "@kestra-io/design-system"

    import TopNavBar from "../layout/TopNavBar.vue"
    import {useStudioControlPlaneStore} from "../../stores/studioControlPlane"

    const store = useStudioControlPlaneStore()
    const targetId = ref("")
    const releaseOpen = ref(false)
    const sourceCommit = ref("")

    const target = computed(() => store.targets.find((candidate) => candidate.id === targetId.value))

    onMounted(async () => {
        const targets = await store.loadTargets()
        targetId.value = targets[0]?.id ?? ""
        await loadTarget()
    })

    async function loadTarget() {
        if (!targetId.value) return
        await store.loadReleases(targetId.value)
    }

    async function prepare() {
        if (!targetId.value || !sourceCommit.value.trim()) return
        await store.prepareRelease(targetId.value, sourceCommit.value.trim())
        sourceCommit.value = ""
        releaseOpen.value = false
    }
</script>

<style scoped lang="scss">
    .studio-page {
        display: flex;
        flex-direction: column;
        gap: var(--ks-spacing-4);
        padding-inline: var(--ks-spacing-5);
    }

    .heading,
    .release-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
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

    .release-row {
        padding-block: var(--ks-spacing-2);
        border-bottom: 1px solid var(--ks-border-subtle);
    }

    .release-row:last-child {
        border-bottom: 0;
    }
</style>
