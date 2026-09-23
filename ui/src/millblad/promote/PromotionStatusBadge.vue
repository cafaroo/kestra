<template>
    <span class="promotion-status" :class="statusClass">
        <span class="promotion-status__dot" aria-hidden="true" />
        {{ label }}
    </span>
</template>

<script setup lang="ts">
    import {computed} from "vue"
    import type {PromotionStatus} from "./model"

    const props = defineProps<{
        status: PromotionStatus
    }>()

    const label = computed(() => props.status.replaceAll("_", " "))

    const statusClass = computed(() => ({
        "is-success": props.status === "IN_SYNC",
        "is-warning": props.status === "OUT_OF_SYNC" || props.status === "DEPLOYING",
        "is-danger": props.status === "FAILED" || props.status === "UNREACHABLE",
        "is-neutral": props.status === "NOT_PROMOTED",
    }))
</script>

<style scoped lang="scss">
.promotion-status {
    display: inline-flex;
    align-items: center;
    gap: var(--ks-spacing-1);
    padding: 0.2rem 0.55rem;
    border: 1px solid var(--ks-border-primary);
    border-radius: 999px;
    color: var(--ks-content-secondary);
    background: var(--ks-background-secondary);
    font-size: 0.75rem;
    font-weight: 600;
    line-height: 1.25rem;
    white-space: nowrap;
}

.promotion-status__dot {
    width: 0.45rem;
    height: 0.45rem;
    border-radius: 50%;
    background: currentColor;
}

.is-success {
    color: var(--ks-content-success);
}

.is-warning {
    color: var(--ks-content-warning);
}

.is-danger {
    color: var(--ks-content-error);
}

.is-neutral {
    color: var(--ks-content-tertiary);
}
</style>
