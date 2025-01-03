package tn.esprit.similator.dtos;

import java.time.LocalDate;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.similator.entity.ClaimStatus;

@Getter
@Setter
@Builder
public class ClaimRequest {

    private Long id;
    private Long userInsuranceId;
    private LocalDate claimDate;
    private String reason;
    private double amountClaimed;
    @Enumerated(EnumType.STRING)
    private ClaimStatus status; // Pending, Approved, Rejected
}
