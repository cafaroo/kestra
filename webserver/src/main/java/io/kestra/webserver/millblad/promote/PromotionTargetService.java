package io.kestra.webserver.millblad.promote;

import java.util.List;

import io.kestra.webserver.millblad.promote.PromotionModels.PromotionTarget;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PromotionTargetService {
    private final PromotionTargetProvider targetProvider;

    @Inject
    public PromotionTargetService(final PromotionTargetProvider targetProvider) {
        this.targetProvider = targetProvider;
    }

    public List<PromotionTarget> list(final String resolvedTenant) {
        return targetProvider.list(resolvedTenant);
    }

    public PromotionTarget require(final String resolvedTenant, final String targetId) {
        return list(resolvedTenant).stream()
            .filter(PromotionTarget::enabled)\n            .filter(target -> target.id().equals(targetId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown promotion target: '%s'".formatted(targetId)));
    }
}