package tn.esprit.similator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.ChallengeParticipation;
import tn.esprit.similator.repository.ChallengeParticipationRepository;
import tn.esprit.similator.repository.ChallengeRepository;
import tn.esprit.similator.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeParticipationService {


    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepo userRepository;

    @Autowired
    public ChallengeParticipationService(ChallengeParticipationRepository challengeParticipationRepository,
                                         ChallengeRepository challengeRepository,
                                         UserRepo userRepository) {
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    public ChallengeParticipation createParticipation(ChallengeParticipation participation) {
        // Définir la date de participation au moment de la création
        participation.setParticipationDate(LocalDateTime.now());
        // Enregistrer la participation
        return challengeParticipationRepository.save(participation);
    }

    public List<ChallengeParticipation> getParticipationsForUser(Long userId) {
        return challengeParticipationRepository.findByUserId(userId);
    }
}
