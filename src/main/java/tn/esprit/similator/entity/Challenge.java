package tn.esprit.similator.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.pif.entity.Transaction;
import tn.esprit.pif.entity.User;

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
    // Nom du challenge


    // Jours minimum de trading
    private int minimumTradingDays;

    // Effet de levier (Leverage)
    private String leverage;

    // Objectif de profit (Profit Target)
    private String profitTarget;

    // Pertes maximales (Max Loss)
    private String maxLoss;

    // Pertes maximales par jour (Max Daily Loss)
    private String maxDailyLoss;

    // Délai de paiement express (Payout Express)
    private String payoutExpress;

    // Booster de profit (Profit Booster)
    private String profitBooster;

    // Trading sur les news (Trade the News)
    private boolean tradeTheNews;
    @Enumerated(EnumType.STRING)
    private ChallengeCategory category;

    // Trading le week-end (Weekend Trading)
    private boolean weekendTrading;

    // Activation de EA (EA Enabled)
    private boolean eaEnabled;

    // Frais remboursables (Refundable Fees)
    private String refundableFees;

    // Dates de début et de fin du challenge
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;


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
