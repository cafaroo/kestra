<template>
    <div class="targets-page">
        <TopNavBar title="Promotion targets" />

        <section class="targets-content">
            <header class="targets-header">
                <div>
                    <p class="eyebrow">Millblad Promote</p>
                    <h2>Promotion targets</h2>
                    <p class="muted">
                        Targets use Forgejo-backed immutable releases. Customer runtimes pull and reconcile locally.
                    </p>
                </div>
                <span class="context-pill">{{ tenantLabel }}</span>
            </header>

            <div v-if="loading" class="state-card">
                Loading promotion targets…
            </div>

            <div v-else-if="error" class="state-card state-card--error" role="alert">
                {{ error }}
            </div>

            <div v-else-if="targets.length === 0" class="state-card">
                No promotion targets are configured for the current context.
            </div>

            <div v-else class="targets-table" role="table" aria-label="Promotion targets">
                <div class="targets-row targets-row--header" role="row">
                    <span>Target</span>
                    <span>Environment</span>
                    <span>Transport</span>
                    <span>Automation</span>
                    <span>Runtime</span>
                    <span>Gate</span>
                    <span>Health</span>
                </div>

                <div
                    v-for="target in targets"
                    :key="target.id"
                    class="targets-row"
                    role="row"
                >
                    <div>
                        <strong>{{ target.displayName }}</strong>
                        <span class="muted">{{ target.runtimeId }}</span>
                    </div>
                    <span>{{ target.environment }}</span>
                    <span>Forgejo release</span>
                    <PromotionStatusBadge :status="target.automationStatus" />
                    <PromotionStatusBadge :status="target.runtimeStatus" />
                    <span>{{ target.confirmationGate ? "Required" : "None" }}</span>
                    <span class="health" :class="'health--' + target.health.toLowerCase()">
                        <span class="health__dot" aria-hidden="true" />
                        {{ target.health }}
                    </span>
                </div>
            </div>

            <aside class="architecture-note">
                <strong>Transport boundary</strong>
                <span>
                    Promote publishes through Forgejo. Studio does not copy definitions directly into a customer Kestra instance.
                </span>
            </aside>
        </section>
    </div>
</template>

<script setup lang="ts">
    import {computed, onMounted, ref} from "vue"
    import {useRoute} from "vue-router"
    import TopNavBar from "../../components/layout/TopNavBar.vue"
    import PromotionStatusBadge from "./PromotionStatusBadge.vue"
    import {usePromoteApi} from "./api"
    import type {PromotionTarget} from "./model"

    const route = useRoute()
    const promoteApi = usePromoteApi()

    const targets = ref<PromotionTarget[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)
    const tenantLabel = computed(() => String(route.params.tenant ?? "current customer"))

    onMounted(loadTargets)

    async function loadTargets() {
        loading.value = true
        error.value = null
        try {
            targets.value = await promoteApi.listTargets()
        } catch (cause) {
            error.value = cause instanceof Error ? cause.message : "Failed to load promotion targets."
        } finally {
            loading.value = false
        }
    }
</script>

<style scoped lang="scss">
.targets-page {
    height: 100%;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

.targets-content {
    display: flex;
    flex: 1;
    min-height: 0;
    flex-direction: column;
    gap: var(--ks-spacing-5);
    padding: var(--ks-spacing-5);
}

.targets-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: var(--ks-spacing-4);
}

h2,
p {
    margin: 0;
}

.eyebrow {
    margin-bottom: var(--ks-spacing-1);
    color: var(--ks-content-secondary);
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.04em;
    text-transform: uppercase;
}

.muted {
    display: block;
    color: var(--ks-content-secondary);
    font-size: 0.875rem;
}

.context-pill {
    padding: 0.35rem 0.65rem;
    border: 1px solid var(--ks-border-primary);
    border-radius: 999px;
    background: var(--ks-background-secondary);
    color: var(--ks-content-secondary);
    font-size: 0.75rem;
    font-weight: 600;
}

.targets-table {
    overflow: hidden;
    border: 1px solid var(--ks-border-primary);
    border-radius: var(--ks-border-radius-large);
    background: var(--ks-background-primary);
}

.targets-row {
    display: grid;
    grid-template-columns: minmax(12rem, 1.4fr) 0.8fr 1fr 1fr 1fr 0.8fr 0.8fr;
    gap: var(--ks-spacing-3);
    align-items: center;
    min-height: 4.25rem;
    padding: var(--ks-spacing-3) var(--ks-spacing-4);
    border-top: 1px solid var(--ks-border-primary);
}

.targets-row:first-child {
    border-top: 0;
}

.targets-row--header {
    min-height: auto;
    background: var(--ks-background-secondary);
    color: var(--ks-content-secondary);
    font-size: 0.75rem;
    font-weight: 700;
}

.targets-row > div {
    display: flex;
    flex-direction: column;
    gap: var(--ks-spacing-1);
}

.health {
    display: inline-flex;
    align-items: center;
    gap: var(--ks-spacing-1);
    font-size: 0.75rem;
    font-weight: 600;
}

.health__dot {
    width: 0.45rem;
    height: 0.45rem;
    border-radius: 50%;
    background: currentColor;
}

.health--healthy {
    color: var(--ks-content-success);
}

.health--degraded {
    color: var(--ks-content-warning);
}

.health--unknown {
    color: var(--ks-content-tertiary);
}

.architecture-note,
.state-card {
    display: flex;
    flex-direction: column;
    gap: var(--ks-spacing-1);
    padding: var(--ks-spacing-4);
    border: 1px solid var(--ks-border-primary);
    border-radius: var(--ks-border-radius-large);
    background: var(--ks-background-secondary);
}

.state-card--error {
    color: var(--ks-content-error);
}

@media (max-width: 1200px) {
    .targets-table {
        overflow-x: auto;
    }

    .targets-row {
        min-width: 64rem;
    }
}
</style>
