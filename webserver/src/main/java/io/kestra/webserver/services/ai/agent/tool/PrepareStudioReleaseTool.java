package io.kestra.webserver.services.ai.agent.tool;

import io.kestra.core.ai.agent.models.AgentToolDomain;
import io.kestra.core.ai.agent.models.AgentToolFamily;
import io.kestra.core.ai.agent.models.AgentWritePolicy;
import io.kestra.core.studio.ReleaseRecord;
import io.kestra.core.studio.StudioControlPlaneService;
import io.kestra.webserver.services.ai.agent.AgentCallContext;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PrepareStudioReleaseTool implements AiPlatformTool {
    private final StudioControlPlaneService service;

    @Inject
    public PrepareStudioReleaseTool(final StudioControlPlaneService service) {
        this.service = service;
    }

    @Override
    public AgentToolDomain domain() {
        return AgentToolDomain.RELEASE;
    }

    @Override
    public AgentToolFamily family() {
        return AgentToolFamily.ACT;
    }

    @Override
    public AgentWritePolicy writePolicy() {
        return AgentWritePolicy.CONFIRM;
    }

    @Tool(
        name = "prepare-studio-release",
        value = "Prepare an immutable release record for a Central Studio target and set that commit as desired state. "
            + "This requires confirmation. It does not claim the runtime is deployed or healthy; deployment is a separate orchestrator step."
    )
    public ReleaseRecord prepare(
        @P(name = "targetId", value = "Release target id") String targetId,
        @P(name = "sourceCommit", value = "Immutable Git commit SHA to release") String sourceCommit,
        final AgentCallContext.Context context) {
        return service.createRelease(context.tenant(), targetId, sourceCommit, "copilot");
    }
}
