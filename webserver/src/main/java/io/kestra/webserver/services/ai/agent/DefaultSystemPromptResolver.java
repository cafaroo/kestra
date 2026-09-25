package io.kestra.webserver.services.ai.agent;

import io.kestra.core.ai.agent.models.AgentMode;
import io.kestra.core.contexts.configuration.StudioConfiguration;

import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * OSS default: uses the built-in per-mode prompt and, in Millblad Central Studio, appends the
 * definition-only operating contract. Custom, per-provider prompts remain replaceable by editions.
 */
@Singleton
public class DefaultSystemPromptResolver implements SystemPromptResolver {
    private static final String STUDIO_SUFFIX = """
        You are operating in Millblad Central Studio.

        - Central Studio is a definition and control plane and contains no customer runtime payload data.
        - The active tenant is supplied by the platform. Never invent, change, or select a tenant yourself.
        - Tenant means customer. Namespace means workflow/business domain. Release target is a separate environment.
        - Executions, logs, runtime KV, customer documents, outputs, and secret values remain inside customer runtimes.
        - Never claim to have read customer runtime data from Central Studio.
        - Secret references may be handled, but secret values must never be returned.
        - Infrastructure edits change desired state only; they do not directly mutate production.
        - Production-impacting release actions must use the platform confirmation flow and immutable source commits.
        """;

    private final StudioConfiguration studioConfiguration;

    @Inject
    public DefaultSystemPromptResolver(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    public String resolve(final AgentMode mode, @Nullable final String providerId, final String defaultPrompt) {
        if (!studioConfiguration.enabled()) {
            return defaultPrompt;
        }
        return defaultPrompt + "\n\n" + STUDIO_SUFFIX;
    }
}
