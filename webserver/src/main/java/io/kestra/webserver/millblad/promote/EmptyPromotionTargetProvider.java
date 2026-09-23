package io.kestra.webserver.millblad.promote;

import java.util.List;

import io.kestra.webserver.millblad.promote.PromotionModels.PromotionTarget;

import jakarta.inject.Singleton;

/**
 * Safe fallback until the tenancy/release-target adapter supplies real targets.
 *
 * Returning no targets is intentional: Promote must never invent deployment targets.
 */
@Singleton
public class EmptyPromotionTargetProvider implements PromotionTargetProvider {
    @Override
    public List<PromotionTarget> list(final String resolvedTenant) {
        return List.of();
    }
}
