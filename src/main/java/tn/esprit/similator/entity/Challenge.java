package tn.esprit.similator.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int minimumTradingDays;
    private String leverage;     // Effet de levier (Leverage)
    private String profitTarget;     // Objectif de profit (Profit Target)
    private String maxLoss;    // Pertes maximales (Max Loss)
    private String maxDailyLoss;    // Pertes maximales par jour (Max Daily Loss)
    private String payoutExpress;     // Délai de paiement express (Payout Express)
    private String profitBooster;    // Booster de profit (Profit Booster)
    private boolean tradeTheNews;    // Trading sur les news (Trade the News)
    private boolean weekendTrading;    // Trading le week-end (Weekend Trading)
    private boolean eaEnabled; // Activation de EA (EA Enabled)
    private String refundableFees;    // Frais remboursables (Refundable Fees)
    // Dates de début et de fin du challenge
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ChallengeCategory category;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChallengeParticipation> challengeParticipations ;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Transaction> transactions;

    @ManyToMany
    @JoinTable(name = "Challenge_users",
            joinColumns = @JoinColumn(name = "challenge_id"),
            inverseJoinColumns = @JoinColumn(name = "users_id"))
    private Set<User> users ;


}
