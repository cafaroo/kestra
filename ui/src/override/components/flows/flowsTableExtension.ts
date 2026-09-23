import type {Component} from "vue"
import {type ColumnConfig} from "@kestra-io/design-system"
import FlowDeployStatusCell from "../../../millblad/promote/FlowDeployStatusCell.vue"

export interface FlowsTableFlowRef {
    id: string;
    namespace: string;
}

export interface FlowsTableExtensionColumn extends ColumnConfig {
    cell: Component;
    header?: Component;
}

export interface FlowsTableExtension {
    columns: FlowsTableExtensionColumn[];
    bulkAction?: Component;
    load?: (flows: FlowsTableFlowRef[]) => void;
}

export function useFlowsTableExtension(): FlowsTableExtension {
    return {
        columns: [
            {
                label: "Deploy",
                prop: "millbladPromotion",
                default: true,
                description: "Promotion status for the selected Millblad target",
                cell: FlowDeployStatusCell,
            },
        ],
    }
}
