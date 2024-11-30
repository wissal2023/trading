package tn.esprit.pif.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pif.entity.Challenge;
@Repository

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}