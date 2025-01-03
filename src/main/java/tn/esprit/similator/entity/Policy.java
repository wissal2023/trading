package tn.esprit.similator.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String premium; // Flat rate or percentage
    private String coverageDetails;
    private double maxCoverageAmount;
    private String conditions;
    private int durationInDays; // Validity duration in days

    @OneToMany(mappedBy = "policy")
    @JsonIgnore
    private List<UserInsurance> userInsurances;

}

