package io.kestra.webserver.millblad.promote;

import java.util.Optional;

import io.kestra.webserver.millblad.promote.PromotionModels.PromotionCandidate;

public interface PromotionCandidateStore {
    void put(String resolvedTenant, PromotionCandidate candidate);

    Optional<PromotionCandidate> find(String resolvedTenant, String candidateId);
}
