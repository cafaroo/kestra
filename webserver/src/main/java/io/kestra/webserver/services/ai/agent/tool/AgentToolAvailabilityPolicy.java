package io.kestra.webserver.services.ai.agent.tool;

public interface AgentToolAvailabilityPolicy {
    boolean isAvailable(ToolCatalog.ToolEntry entry, String tenant);
}
