package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.ChallengeParticipation;

import java.util.List;

@Repository
public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    // Méthode pour vérifier si un utilisateur participe déjà à un challenge
    //boolean existsByUserIdAndChallenge(Long userId, Challenge challenge);

    List<ChallengeParticipation> findByUserId(Long userId);
    List<ChallengeParticipation> findByChallengeId(Long challengeId);

    // Optionnel : Vous pouvez ajouter d'autres méthodes selon vos besoins

}