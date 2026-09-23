package io.kestra.webserver.millblad.promote;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.kestra.webserver.millblad.promote.PromotionModels.ArtefactKind;
import io.kestra.webserver.millblad.promote.PromotionModels.CandidateStatus;
import io.kestra.webserver.millblad.promote.PromotionModels.CompatibilityStatus;
import io.kestra.webserver.millblad.promote.PromotionModels.CreatePromotionCandidateRequest;
import io.kestra.webserver.millblad.promote.PromotionModels.PromotionStatus;
import io.kestra.webserver.millblad.promote.PromotionModels.PromotionTarget;
import io.kestra.webserver.millblad.promote.PromotionModels.RuntimeHealth;
import io.kestra.webserver.millblad.promote.PromotionModels.TransportType;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionCandidateServiceTest {
    private final PromotionTargetProvider provider = ignored -> List.of(
        new PromotionTarget(
            "staging",
            "Staging",
            "staging",
            TransportType.FORGEJO_RELEASE,
            "staging",
            false,
            true,
            PromotionStatus.NOT_PROMOTED,
            PromotionStatus.NOT_PROMOTED,
            RuntimeHealth.UNKNOWN
        ),
        new PromotionTarget(
            "production",
            "Production",
            "production",
            TransportType.FORGEJO_RELEASE,
            "production",
            true,
            true,
            PromotionStatus.NOT_PROMOTED,
            PromotionStatus.NOT_PROMOTED,
            RuntimeHealth.UNKNOWN
        )
    );

    private final PromotionCandidateService service =
        new PromotionCandidateService(new PromotionTargetService(provider), new InMemoryPromotionCandidateStore());

    @Test
    void shouldCreateCandidateWithTargetGate() {
        var candidate = service.create(
            "tenant-a",
            new CreatePromotionCandidateRequest(
                ArtefactKind.AUTOMATION,
                "ata-painting",
                "24",
                "production"
            )
        );

        assertThat(candidate.artefactKind()).isEqualTo(ArtefactKind.AUTOMATION);
        assertThat(candidate.artefactId()).isEqualTo("ata-painting");
        assertThat(candidate.sourceRevision()).isEqualTo("24");
        assertThat(candidate.targetId()).isEqualTo("production");
        assertThat(candidate.confirmationRequired()).isTrue();
        assertThat(candidate.compatibility()).isEqualTo(CompatibilityStatus.UNKNOWN);
        assertThat(candidate.status()).isEqualTo(CandidateStatus.CREATED);
    }

    @Test
    void shouldKeepCandidateLookupInsideResolvedTenantContext() {
        var candidate = service.create(
            "tenant-a",
            new CreatePromotionCandidateRequest(
                ArtefactKind.RUNTIME,
                "runtime",
                "18",
                "staging"
            )
        );

        assertThat(service.find("tenant-a", candidate.id())).contains(candidate);
        assertThat(service.find("tenant-b", candidate.id())).isEmpty();
    }
}