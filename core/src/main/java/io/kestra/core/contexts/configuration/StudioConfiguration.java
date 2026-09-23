package io.kestra.core.contexts.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

@ConfigurationProperties("millblad.studio")
public record StudioConfiguration(
    @Bindable(defaultValue = "false") boolean enabled,
    @Bindable(defaultValue = "false") boolean definitionOnly) {
}
