package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.similator.entity.Policy;


public interface AssuranceRepository extends JpaRepository<Policy,Long> {
}
