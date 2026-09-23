package io.kestra.webserver.services.ai.agent.tool;

import java.util.List;

import io.kestra.core.ai.agent.models.AgentToolDomain;
import io.kestra.core.ai.agent.models.AgentToolFamily;
import io.kestra.core.ai.agent.models.AgentWritePolicy;
import io.kestra.core.studio.ReleaseTarget;
import io.kestra.core.studio.StudioControlPlaneService;
import io.kestra.webserver.services.ai.agent.AgentCallContext;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ListReleaseTargetsTool implements AiPlatformTool {
    private final StudioControlPlaneService service;

    @Inject
    public ListReleaseTargetsTool(final StudioControlPlaneService service) {
        this.service = service;
    }

    @Override
    public AgentToolDomain domain() {
        return AgentToolDomain.RELEASE;
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
        name = "list-release-targets",
        value = "List Central Studio release targets for the active customer tenant. Read-only. "
            + "Returns target identifiers, runtime URLs and desired/deployed commit metadata; it never returns execution payload data."
    )
    public Result list(final AgentCallContext.Context context) {
        return new Result(service.listTargets(context.tenant()));
    }

    public record Result(List<ReleaseTarget> targets) {
    }
}
