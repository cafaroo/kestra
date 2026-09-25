package io.kestra.webserver.services.ai.agent.tool;

import io.kestra.core.ai.agent.models.AgentToolDomain;
import io.kestra.core.contexts.configuration.StudioConfiguration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultAgentToolAvailabilityPolicy implements AgentToolAvailabilityPolicy {
    private final StudioConfiguration studioConfiguration;

    @Inject
    public DefaultAgentToolAvailabilityPolicy(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    public boolean isAvailable(final ToolCatalog.ToolEntry entry, final String tenant) {
        if (!studioConfiguration.enabled() || !studioConfiguration.definitionOnly()) {
            return true;
        }

        return entry.domain() != AgentToolDomain.RUNTIME;
    }
}
