package io.kestra.webserver.millblad.promote;

import java.util.List;

import io.kestra.webserver.millblad.promote.PromotionModels.PromotionTarget;

/**
 * Supplies promotion targets for an already-resolved tenant/customer context.
 *
 * The tenancy workstream owns how that context maps to configured targets. Promote only consumes it.
 */
@FunctionalInterface
public interface PromotionTargetProvider {
    List<PromotionTarget> list(String resolvedTenant);
}
