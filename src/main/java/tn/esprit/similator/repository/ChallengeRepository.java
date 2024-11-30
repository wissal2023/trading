package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.Challenge;

@Repository

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

}