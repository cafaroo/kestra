<template>
    <KsEditor
        class="change-diff"
        :style="{height}"
        v-bind="editorBindings"
        :options="{diffSideBySide: sideBySide}"
        :modelValue="newValue"
        :original="oldValue"
        readOnly
        :lang="lang"
    />
</template>

<script setup lang="ts">
    import {withDefaults} from "vue"
    import {KsEditor} from "@kestra-io/design-system"
    import {useEditorBindings} from "../../composables/useEditorBindings"

    withDefaults(defineProps<{
        /** Current persisted/target content. Empty string renders the proposal as a pure addition. */
        oldValue: string
        /** Proposed content. */
        newValue: string
        /** Monaco language used for syntax highlighting. */
        lang?: string
        /** Render Monaco's side-by-side diff rather than the compact inline view. */
        sideBySide?: boolean
        /** Explicit editor height so callers can fit the diff to their review surface. */
        height?: string
    }>(), {
        lang: "yaml",
        sideBySide: false,
        height: "20rem",
    })

    const editorBindings = useEditorBindings()
</script>

<style scoped>
    .ks-editor.change-diff {
        min-height: 8rem;
    }
</style>
