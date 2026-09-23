package io.kestra.webserver.services.ai.agent.tool;

import java.util.List;

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
public class ListStudioReleasesTool implements AiPlatformTool {
    private final StudioControlPlaneService service;

    @Inject
    public ListStudioReleasesTool(final StudioControlPlaneService service) {
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

    @Tool(name = "list-studio-releases", value = "List immutable Central Studio release records for a target. Read-only.")
    public Result list(
        @P(name = "targetId", value = "Release target id") String targetId,
        final AgentCallContext.Context context) {
        return new Result(service.listReleases(context.tenant(), targetId));
    }

    public record Result(List<ReleaseRecord> releases) {
    }
}
