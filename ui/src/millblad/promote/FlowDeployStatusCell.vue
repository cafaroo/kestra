<template>
    <div class="deploy-cell" @click.stop>
        <span class="deploy-cell__placeholder">—</span>
        <span class="deploy-cell__hint">Select target</span>
    </div>
</template>

<script setup lang="ts">
    defineProps<{
        row: {
            id: string
            namespace: string
        }
    }>()
</script>

<style scoped>
.deploy-cell {
    display: flex;
    align-items: center;
    gap: var(--ks-spacing-2);
    min-width: 8rem;
    color: var(--ks-content-secondary);
}

.deploy-cell__placeholder {
    font-weight: 700;
}

.deploy-cell__hint {
    font-size: 0.75rem;
}
</style>
