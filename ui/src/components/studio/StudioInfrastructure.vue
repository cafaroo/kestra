<template>
    <TopNavBar :title="$t('setup.survey.use_case_infrastructure')" hideMainIcon>
        <template #actions>
            <KsSelect v-model="targetId" size="small" @change="load">
                <KsOption
                    v-for="target in store.targets"
                    :key="target.id"
                    :label="target.name"
                    :value="target.id"
                />
            </KsSelect>
        </template>
    </TopNavBar>

    <KsSplitter v-if="definition" class="infrastructure-layout">
        <KsSplitterPanel size="72%" min="40%">
            <div class="canvas">
                <VueFlow
                    id="studio-infrastructure"
                    :nodes="flowNodes"
                    :edges="flowEdges"
                    :nodesDraggable="false"
                    :nodesConnectable="false"
                    :fitViewOnInit="true"
                    @nodeClick="({node}) => selectNode(node.id)"
                    @paneClick="selectNode(undefined)"
                >
                    <Background :gap="24" :size="1" />
                    <template #node-infrastructure="{data}">
                        <div class="infra-node">
                            <div class="node-heading">
                                <KsText class="node-name">{{ data.name }}</KsText>
                                <KsTag v-if="data.status" size="xs">{{ data.status }}</KsTag>
                            </div>
                            <KsText size="small" class="node-kind">{{ data.kind }}</KsText>
                            <KsText v-if="data.version" size="small" class="mono">{{ data.version }}</KsText>
                        </div>
                    </template>
                </VueFlow>
            </div>
        </KsSplitterPanel>

        <KsSplitterPanel min="320px">
            <div v-if="selected" class="inspector">
                <KsText tag="h3" size="large">{{ selected.name }}</KsText>
                <KsTag size="small">{{ selected.kind }}</KsTag>

                <dl class="details">
                    <template v-if="selected.image">
                        <dt>{{ $t("source") }}</dt>
                        <dd class="mono">{{ selected.image }}</dd>
                    </template>
                    <template v-if="selected.version">
                        <dt>{{ $t("revision") }}</dt>
                        <dd class="mono">{{ selected.version }}</dd>
                    </template>
                    <template v-for="(value, key) in selected.metadata" :key="key">
                        <dt>{{ key }}</dt>
                        <dd>{{ value }}</dd>
                    </template>
                </dl>
            </div>
        </KsSplitterPanel>
    </KsSplitter>
</template>

<script setup lang="ts">
    import {computed, onMounted, ref} from "vue"
    import {useRoute, useRouter} from "vue-router"
    import {VueFlow, type Edge, type Node} from "@vue-flow/core"
    import {Background} from "@vue-flow/background"
    import {
        KsOption,
        KsSelect,
        KsSplitter,
        KsSplitterPanel,
        KsTag,
        KsText,
    } from "@kestra-io/design-system"

    import TopNavBar from "../layout/TopNavBar.vue"
    import {useStudioControlPlaneStore} from "../../stores/studioControlPlane"

    const store = useStudioControlPlaneStore()
    const route = useRoute()
    const router = useRouter()
    const targetId = ref("")
    const selectedId = ref<string>()

    onMounted(async () => {
        const targets = await store.loadTargets()
        const queryTarget = typeof route.query.target === "string" ? route.query.target : undefined
        targetId.value = targets.some((target) => target.id === queryTarget)
            ? queryTarget!
            : (targets[0]?.id ?? "")
        const queryNode = typeof route.query.node === "string" ? route.query.node : undefined
        selectedId.value = queryNode
        await load(false)
    })

    async function load(clearNode = true) {
        if (clearNode) selectedId.value = undefined
        if (!targetId.value) return
        await store.loadInfrastructure(targetId.value)
        await router.replace({
            query: {
                ...route.query,
                target: targetId.value,
                node: selectedId.value,
            },
        })
    }

    async function selectNode(id: string | undefined) {
        selectedId.value = id
        await router.replace({
            query: {
                ...route.query,
                target: targetId.value || undefined,
                node: id,
            },
        })
    }

    const definition = computed(() => store.infrastructure)
    const selected = computed(() => definition.value?.nodes.find((node) => node.id === selectedId.value))

    const flowNodes = computed<Node[]>(() => {
        if (!definition.value) return []
        const groupOrder = new Map(definition.value.boundaries.map((group, index) => [group.id, index]))
        const counters = new Map<string, number>()

        return definition.value.nodes.map((node) => {
            const row = counters.get(node.groupId) ?? 0
            counters.set(node.groupId, row + 1)
            const column = groupOrder.get(node.groupId) ?? 0
            return {
                id: node.id,
                type: "infrastructure",
                position: {x: 80 + column * 320, y: 80 + row * 120},
                data: node,
            }
        })
    })

    const flowEdges = computed<Edge[]>(() => definition.value?.edges.map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        label: edge.label ?? edge.kind,
        type: "smoothstep",
    })) ?? [])
</script>

<style scoped lang="scss">
    .infrastructure-layout {
        height: calc(100vh - 60px);
    }

    .canvas {
        width: 100%;
        height: 100%;
        background: var(--ks-bg-base);
    }

    .infra-node {
        width: 15rem;
        padding: var(--ks-spacing-3);
        border: 1px solid var(--ks-border-default);
        border-radius: var(--ks-radius-base);
        background: var(--ks-bg-surface);
        box-shadow: var(--ks-shadow-sm);
    }

    .node-heading {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--ks-spacing-2);
    }

    .node-name {
        font-family: var(--ks-font-family-mono);
        font-weight: var(--ks-font-weight-semibold);
    }

    .node-kind {
        color: var(--ks-text-secondary);
    }

    .mono {
        font-family: var(--ks-font-family-mono);
    }

    .inspector {
        display: flex;
        flex-direction: column;
        gap: var(--ks-spacing-3);
        padding: var(--ks-spacing-4);
    }

    .details {
        display: grid;
        grid-template-columns: 7rem 1fr;
        gap: var(--ks-spacing-2) var(--ks-spacing-3);
        padding-top: var(--ks-spacing-3);
        border-top: 1px solid var(--ks-border-subtle);
    }

    .details dt {
        color: var(--ks-text-secondary);
    }

    .details dd {
        margin: 0;
        overflow-wrap: anywhere;
    }
</style>
