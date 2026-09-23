package io.kestra.webserver.services.ai.agent.tool;

import io.kestra.core.ai.agent.models.AgentToolDomain;
import io.kestra.core.ai.agent.models.AgentToolFamily;
import io.kestra.core.ai.agent.models.AgentWritePolicy;
import io.kestra.core.studio.InfrastructureDefinition;
import io.kestra.core.studio.StudioControlPlaneService;
import io.kestra.webserver.services.ai.agent.AgentCallContext;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ReadInfrastructureTool implements AiPlatformTool {
    private final StudioControlPlaneService service;

    @Inject
    public ReadInfrastructureTool(final StudioControlPlaneService service) {
        this.service = service;
    }

    @Override
    public AgentToolDomain domain() {
        return AgentToolDomain.INFRASTRUCTURE;
    }

    @Override
    public AgentToolFamily family() {
        return AgentToolFamily.READ;
    }

    @Override
    public AgentWritePolicy writePolicy() {
        return AgentWritePolicy.AUTO;
    }

    @Tool(
        name = "read-infrastructure",
        value = "Read Central Studio infrastructure desired state for one release target. Read-only. "
            + "Returns definition metadata, nodes, boundaries and connections only; no customer logs, execution payloads or secret values."
    )
    public InfrastructureDefinition read(
        @P(name = "targetId", value = "Release target id, for example test or production") String targetId,
        final AgentCallContext.Context context) {
        return service.findInfrastructure(context.tenant(), targetId)
            .orElseThrow(() -> new IllegalArgumentException("No infrastructure definition for target '" + targetId + "'"));
    }
}
