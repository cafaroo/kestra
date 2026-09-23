<template>
    <ChangeDiff
        class="diff-view"
        data-test="copilot-diff"
        :oldValue="oldValue"
        :newValue="newValue"
    />
</template>

<script setup lang="ts">
    import ChangeDiff from "../../utils/ChangeDiff.vue"

    defineProps<{
        /** The current content ("before"). Empty when there's nothing to diff against (e.g. a brand-new
         *  flow) — Monaco's diff editor then renders every line of `newValue` as an addition. */
        oldValue: string
        newValue: string
    }>()
</script>
