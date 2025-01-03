package tn.esprit.similator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.Claim;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByUserInsuranceId(Long userInsuranceId);
}
