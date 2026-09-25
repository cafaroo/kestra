package io.kestra.webserver.services.ai.agent.tool;

import io.kestra.core.ai.agent.models.AgentToolDomain;
import io.kestra.core.ai.agent.models.AgentToolFamily;
import io.kestra.core.ai.agent.models.AgentWritePolicy;

/**
 * A native platform tool — a deterministic platform call. This base declares no authorization; a
 * replacement can associate each tool with a permission that the {@link AgentToolPermissionEvaluator}
 * consumes at {@link ToolCatalog#dispatch}.
 */
public interface AiPlatformTool extends AiTool {
    default AgentToolDomain domain() {
        return AgentToolDomain.DEFINITION;
    }

    AgentToolFamily family();

    AgentWritePolicy writePolicy();
}
