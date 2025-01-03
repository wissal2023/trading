package tn.esprit.similator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.esprit.similator.entity.UserInsurance;

@Repository
public interface UserInsuranceRepository extends JpaRepository<UserInsurance, Long> {
    List<UserInsurance> findByUserId(Long userId);

    boolean existsByPolicyId(Long policyId);

    boolean existsByUserIdAndPolicyId(Long userId, Long policyId);
}
