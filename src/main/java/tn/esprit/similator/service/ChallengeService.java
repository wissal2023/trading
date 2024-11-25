package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pif.entity.Challenge;
import tn.esprit.pif.repository.ChallengeRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ChallengeService {
    private ChallengeRepository challengeRepository;

    public Challenge createChallenge(Challenge challenge) {
        // Logique de validation (ex. vérifier que la date de fin est après la date de début)
        if (challenge.getEndDate().isBefore(challenge.getStartDate())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
        return challengeRepository.save(challenge);
    }

   /* public Challenge createChallenge(Challenge c) {
        return challengeRepository.save(c);
    }*/

    // Récupérer tous les challenges
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    // Mettre à jour un challenge
    public Challenge updateChallenge(Long id, Challenge challengeDetails) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Challenge non trouvé."));
        // Mise à jour des propriétés du challenge
        challenge.setCategory(challengeDetails.getCategory());
        challenge.setMinimumTradingDays(challengeDetails.getMinimumTradingDays());
        // etc. pour les autres propriétés
        return challengeRepository.save(challenge);
    }

    // Supprimer un challenge
    public void deleteChallenge(Long id) {
        challengeRepository.deleteById(id);
    }

}

