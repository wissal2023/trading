package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChallengeParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private Long userId; // Identifiant de l'utilisateur qui participe au challenge
    private LocalDateTime participationDate;
    private boolean active; // Si la participation est toujours en cours ou terminée
    private Double totalProfit; // Exemple de métrique supplémentaire pour suivre la performance du participant

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "challenge_id") // Référence à la colonne challenge_id
    private Challenge challenge;


    // Constructeur personnalisé (ajoutez les attributs requis)
    public ChallengeParticipation(User user, Challenge challenge, LocalDateTime now, boolean active) {
        this.userId = user.getId(); // Assurez-vous que l'utilisateur a une méthode getId()
        this.challenge = challenge;
        this.participationDate = now;
        this.active = active;
    }
}
