package tn.esprit.similator.dtos;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.similator.entity.Claim;
import tn.esprit.similator.entity.UserInsuranceStatus;

@Getter
@Setter
@Builder
public class UserInsuranceRequest {

    private Long id;
    private Long userId;
    private Long policyId;
    private Long linkedPortfolioId;
    private List<Claim> claims;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private UserInsuranceStatus status; // Active, Expired, Claimed
    
}
