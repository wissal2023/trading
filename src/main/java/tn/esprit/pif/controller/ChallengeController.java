package tn.esprit.pif.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pif.entity.Challenge;
import tn.esprit.pif.entity.ChallengeParticipation;
import tn.esprit.pif.service.ChallengeParticipationService;
import tn.esprit.pif.service.ChallengeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@AllArgsConstructor

@RequestMapping("/api/challenges")
public class ChallengeController {
    @Autowired
    private ChallengeService challengeService;
    ChallengeParticipationService challengeParticipationService;


    @PostMapping("/create")
    public ResponseEntity<?> createChallenge(@Valid @RequestBody Challenge challenge) {
         final Logger log = LoggerFactory.getLogger(ChallengeController.class);

        try {
            if (challenge.getEndDate() == null) {
                return ResponseEntity.badRequest().body("La date de fin ne peut pas être nulle");
            }
            // Vérification que la date de fin est bien après la date de début
            if (challenge.getEndDate().isBefore(challenge.getStartDate())) {
                return ResponseEntity.badRequest().body("La date de fin doit être après la date de début");
            }

            // Appel au service pour créer le challenge
            Challenge savedChallenge = challengeService.createChallenge(challenge);
            return ResponseEntity.ok(savedChallenge);

        } catch (IllegalArgumentException e) {
            // Log pour l'exception spécifique
            log.error("Erreur de validation: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log pour l'exception générique
            log.error("Erreur générale lors de la création du challenge: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de la création du challenge");
        }
    }

    /* @PostMapping("/{challengeId}/participate")
    public ResponseEntity<ChallengeParticipation> participate(
            @PathVariable Long challengeId,
            @RequestParam Long userId) {
        ChallengeParticipation participation = challengeParticipationService.joinChallenge(userId, challengeId) ;
        return ResponseEntity.ok(participation);
    }*/
   @GetMapping("/list")
   public ResponseEntity<List<Challenge>> getChallenges() {
       List<Challenge> challenges = challengeService.getAllChallenges();
       return ResponseEntity.ok(challenges);
   }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteChallenge(@PathVariable Long id) {
        try {
            challengeService.deleteChallenge(id);
            return ResponseEntity.ok().body("Challenge supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression du challenge.");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateChallenge(@PathVariable Long id, @Valid @RequestBody Challenge challengeDetails) {
        try {
            Challenge updatedChallenge = challengeService.updateChallenge(id, challengeDetails);
            return ResponseEntity.ok(updatedChallenge);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du challenge.");
        }
    }




}
