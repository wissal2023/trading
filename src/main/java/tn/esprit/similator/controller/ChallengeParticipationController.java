package tn.esprit.similator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.entity.ChallengeParticipation;
import tn.esprit.similator.service.ChallengeParticipationService;

import java.util.List;

@RestController
@RequestMapping("/api/challenge-participation")
public class ChallengeParticipationController {
    private ChallengeParticipationService participationService;


    @PostMapping("/sign")
    public ResponseEntity<ChallengeParticipation> createParticipation(@RequestBody ChallengeParticipation participation) {
        ChallengeParticipation createdParticipation = participationService.createParticipation(participation);
        return new ResponseEntity<>(createdParticipation, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public List<ChallengeParticipation> getParticipationsForUser(@PathVariable Long userId) {
        return participationService.getParticipationsForUser(userId);
    }
}
