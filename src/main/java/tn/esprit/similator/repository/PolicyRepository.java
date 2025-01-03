package tn.esprit.similator.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import tn.esprit.similator.entity.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    @Query("SELECT p FROM Policy p JOIN UserInsurance ui ON p.id = ui.policy.id GROUP BY p.id ORDER BY COUNT(ui.id) DESC")
    List<Policy> findMostPurchasedPolicy(Pageable pageable);

    @Query("SELECT p FROM Policy p WHERE p.maxCoverageAmount > 0 ORDER BY (p.maxCoverageAmount / (p.durationInDays)) DESC")
    List<Policy> findBestPolicyInTermsOfValue(Pageable pageable);
}
