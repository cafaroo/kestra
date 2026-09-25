package io.kestra.webserver.services.ai.agent;

import org.junit.jupiter.api.Test;

import io.kestra.core.ai.agent.models.AgentMode;
import io.kestra.core.contexts.configuration.StudioConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSystemPromptResolverTest {

    @Test
    void shouldReturnBuiltInPromptOutsideStudio() {
        DefaultSystemPromptResolver resolver = new DefaultSystemPromptResolver(new StudioConfiguration(false, false));

        assertThat(resolver.resolve(AgentMode.EDIT, "gpt", "built-in")).isEqualTo("built-in");
        assertThat(resolver.resolve(AgentMode.ASK, null, "built-in")).isEqualTo("built-in");
    }

    @Test
    void shouldAppendStudioContractInsideStudio() {
        DefaultSystemPromptResolver resolver = new DefaultSystemPromptResolver(new StudioConfiguration(true, true));

        assertThat(resolver.resolve(AgentMode.ASK, null, "built-in"))
            .startsWith("built-in")
            .contains("Millblad Central Studio")
            .contains("active tenant is supplied by the platform")
            .contains("secret values must never be returned");
    }
}
