<template>
    <section class="promote-page">
        <header class="promote-header">
            <div>
                <p class="eyebrow">Millblad Promote</p>
                <h2>Promote automation</h2>
                <p class="muted">
                    {{ flowRef }} is promoted as an Automation Release through Forgejo-backed immutable releases.
                </p>
            </div>
            <span class="transport-pill">Forgejo Release</span>
        </header>

        <div class="source-card">
            <div>
                <span class="label">Source</span>
                <strong>Studio revision {{ sourceRevision }}</strong>
                <span class="muted">Current authored definition</span>
            </div>
            <div>
                <span class="label">Automation</span>
                <strong>{{ automationId }}</strong>
                <span class="muted">Flow-backed automation until the Automation manifest resolver lands</span>
            </div>
        </div>

        <div class="targets-heading">
            <div>
                <h3>Promotion targets</h3>
                <p class="muted">Choose the environment to compare and promote to.</p>
            </div>
            <span v-if="loadingTargets" class="muted">Loading targets…</span>
        </div>

        <div v-if="loadError" class="error-banner" role="alert">
            {{ loadError }}
        </div>

        <div v-else-if="targets.length === 0 && !loadingTargets" class="empty-state">
            No promotion targets are configured for the current context.
        </div>

        <div v-else class="target-grid">
            <button
                v-for="target in targets"
                :key="target.id"
                type="button"
                class="target-card"
                :class="{selected: selectedTargetId === target.id}"
                @click="selectTarget(target.id)"
            >
                <div class="target-card__top">
                    <div>
                        <strong>{{ target.displayName }}</strong>
                        <span class="muted">{{ target.environment }}</span>
                    </div>
                    <PromotionStatusBadge :status="target.automationStatus" />
                </div>

                <dl>
                    <div>
                        <dt>Transport</dt>
                        <dd>Forgejo release</dd>
                    </div>
                    <div>
                        <dt>Confirmation gate</dt>
                        <dd>{{ target.confirmationGate ? "Required" : "Not required" }}</dd>
                    </div>
                    <div>
                        <dt>Runtime</dt>
                        <dd>{{ target.runtimeId }}</dd>
                    </div>
                </dl>
            </button>
        </div>

        <div v-if="selectedTarget" class="candidate-card">
            <div class="candidate-card__header">
                <div>
                    <span class="label">Promotion candidate</span>
                    <h3>{{ selectedTarget.displayName }}</h3>
                </div>
                <PromotionStatusBadge :status="selectedTarget.automationStatus" />
            </div>

            <div class="candidate-grid">
                <div>
                    <span class="label">Diff</span>
                    <strong>Automation release comparison</strong>
                    <span class="muted">The next backend slice adds structured flow, asset and model diffing.</span>
                </div>
                <div>
                    <span class="label">Runtime compatibility</span>
                    <strong>Preflight required</strong>
                    <span class="muted">Laya, MCP, plugin and capability requirements are checked before publish.</span>
                </div>
                <div>
                    <span class="label">Delivery</span>
                    <strong>Pull-based</strong>
                    <span class="muted">Studio publishes an immutable release; the customer runtime reconciles locally.</span>
                </div>
            </div>

            <div v-if="candidate" class="candidate-created">
                <div>
                    <span class="label">Candidate created</span>
                    <strong>{{ candidate.id }}</strong>
                </div>
                <div>
                    <span class="label">Status</span>
                    <strong>{{ candidate.status }}</strong>
                </div>
                <div>
                    <span class="label">Compatibility</span>
                    <strong>{{ candidate.compatibility }}</strong>
                </div>
            </div>

            <div v-if="candidateError" class="error-banner" role="alert">
                {{ candidateError }}
            </div>

            <footer class="candidate-actions">
                <button type="button" class="secondary" @click="selectedTargetId = null">Cancel</button>
                <button
                    type="button"
                    class="primary"
                    :disabled="creatingCandidate"
                    @click="createPromotionCandidate"
                >
                    {{ creatingCandidate ? "Creating candidate…" : "Promote" }}
                </button>
            </footer>
        </div>
    </section>
</template>

<script setup lang="ts">
    import {computed, onMounted, ref} from "vue"
    import {useRoute} from "vue-router"
    import PromotionStatusBadge from "./PromotionStatusBadge.vue"
    import {usePromoteApi, type PromotionCandidate} from "./api"
    import type {PromotionTarget} from "./model"

    const route = useRoute()
    const promoteApi = usePromoteApi()

    const targets = ref<PromotionTarget[]>([])
    const selectedTargetId = ref<string | null>(null)
    const loadingTargets = ref(false)
    const creatingCandidate = ref(false)
    const loadError = ref<string | null>(null)
    const candidateError = ref<string | null>(null)
    const candidate = ref<PromotionCandidate | null>(null)

    const selectedTarget = computed(() => targets.value.find(target => target.id === selectedTargetId.value) ?? null)
    const flowRef = computed(() => `${String(route.params.namespace ?? "")} / ${String(route.params.id ?? "")}`)
    const sourceRevision = computed(() => String(route.query.revision ?? "latest"))

    // Temporary bridge until the first-class Automation manifest/resolver is implemented:
    // a flow promotes an Automation whose id defaults to the flow id.
    const automationId = computed(() => String(route.params.id ?? "automation"))

    onMounted(loadTargets)

    async function loadTargets() {
        loadingTargets.value = true
        loadError.value = null
        try {
            targets.value = await promoteApi.listTargets()
            selectedTargetId.value = targets.value[targets.value.length - 1]?.id ?? null
        } catch (error) {
            loadError.value = error instanceof Error ? error.message : "Failed to load promotion targets."
        } finally {
            loadingTargets.value = false
        }
    }

    function selectTarget(targetId: string) {
        selectedTargetId.value = targetId
        candidate.value = null
        candidateError.value = null
    }

    async function createPromotionCandidate() {
        if (!selectedTarget.value) return

        creatingCandidate.value = true
        candidateError.value = null
        candidate.value = null
        try {
            candidate.value = await promoteApi.createCandidate({
                artefactKind: "AUTOMATION",
                artefactId: automationId.value,
                sourceRevision: sourceRevision.value,
                targetId: selectedTarget.value.id,
            })
        } catch (error) {
            candidateError.value = error instanceof Error ? error.message : "Failed to create promotion candidate."
        } finally {
            creatingCandidate.value = false
        }
    }
</script>

<style scoped lang="scss">
.promote-page {
    display: flex;
    flex-direction: column;
    gap: var(--ks-spacing-5);
    padding: var(--ks-spacing-5);
}

.promote-header,
.source-card,
.candidate-card {
    border: 1px solid var(--ks-border-primary);
    border-radius: var(--ks-border-radius-large);
    background: var(--ks-background-primary);
}

.promote-header {
    display: flex;
    justify-content: space-between;
    gap: var(--ks-spacing-4);
    padding: var(--ks-spacing-5);
}

h2,
h3,
p {
    margin: 0;
}

.eyebrow,
.label {
    display: block;
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

.transport-pill {
    align-self: flex-start;
    padding: 0.35rem 0.65rem;
    border-radius: 999px;
    background: var(--ks-background-secondary);
    color: var(--ks-content-secondary);
    font-size: 0.75rem;
    font-weight: 600;
}

.source-card {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: var(--ks-spacing-5);
    padding: var(--ks-spacing-4);
}

.source-card > div,
.candidate-grid > div,
.candidate-created > div {
    display: flex;
    flex-direction: column;
    gap: var(--ks-spacing-1);
}

.targets-heading {
    display: flex;
    align-items: end;
    justify-content: space-between;
}

.target-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--ks-spacing-3);
}

.target-card {
    appearance: none;
    width: 100%;
    padding: var(--ks-spacing-4);
    border: 1px solid var(--ks-border-primary);
    border-radius: var(--ks-border-radius-large);
    background: var(--ks-background-primary);
    color: inherit;
    text-align: left;
    cursor: pointer;
}

.target-card.selected {
    border-color: var(--ks-border-active);
    box-shadow: 0 0 0 1px var(--ks-border-active);
}

.target-card__top,
.candidate-card__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: var(--ks-spacing-3);
}

.target-card__top > div {
    display: flex;
    flex-direction: column;
    gap: var(--ks-spacing-1);
}

dl {
    display: grid;
    gap: var(--ks-spacing-2);
    margin: var(--ks-spacing-4) 0 0;
}

dl > div {
    display: flex;
    justify-content: space-between;
    gap: var(--ks-spacing-3);
}

dt {
    color: var(--ks-content-secondary);
}

dd {
    margin: 0;
    font-weight: 600;
}

.candidate-card {
    padding: var(--ks-spacing-4);
}

.candidate-grid,
.candidate-created {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--ks-spacing-4);
    margin-top: var(--ks-spacing-4);
    padding-top: var(--ks-spacing-4);
    border-top: 1px solid var(--ks-border-primary);
}

.candidate-created {
    padding: var(--ks-spacing-4);
    border: 1px solid var(--ks-border-primary);
    border-radius: var(--ks-border-radius-large);
    background: var(--ks-background-secondary);
}

.candidate-actions {
    display: flex;
    justify-content: flex-end;
    gap: var(--ks-spacing-2);
    margin-top: var(--ks-spacing-5);
}

.candidate-actions button {
    padding: 0.55rem 1rem;
    border-radius: var(--ks-border-radius);
    font-weight: 600;
}

.secondary {
    border: 1px solid var(--ks-border-primary);
    background: var(--ks-background-primary);
    color: var(--ks-content-primary);
}

.primary {
    border: 1px solid var(--ks-border-active);
    background: var(--ks-background-action);
    color: var(--ks-content-on-action);
}

.primary:disabled {
    opacity: 0.55;
    cursor: not-allowed;
}

.error-banner,
.empty-state {
    padding: var(--ks-spacing-4);
    border: 1px solid var(--ks-border-primary);
    border-radius: var(--ks-border-radius-large);
    background: var(--ks-background-secondary);
}

.error-banner {
    color: var(--ks-content-error);
}

@media (max-width: 1100px) {
    .target-grid,
    .candidate-grid,
    .candidate-created {
        grid-template-columns: 1fr;
    }
}
</style>