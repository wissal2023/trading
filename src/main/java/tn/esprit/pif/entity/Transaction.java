package tn.esprit.pif.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

import static tn.esprit.pif.entity.Status.COMPLETED;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   /* @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // L'utilisateur qui a effectué la transaction*/
   @ManyToOne(optional = false)
   @JoinColumn(name = "challenge_id", nullable = false)
   @JsonBackReference
   private Challenge challenge;





    private Double price; // Montant de la transaction
    private String currency; // Devise de la transaction, par exemple "USD", "EUR", "BTC"
    private Date transactionDate;
    private Double quantity;// Date de la transaction
    @Enumerated(EnumType.STRING)
    private Type type; // Type de transaction: "BUY", "SELL", etc.
    @Enumerated(EnumType.STRING)
    private Status status;
    private Double totalAmount; // Statut de la transaction: "PENDING", "COMPLETED", "FAILED", etc.
    // Attributs supplémentaires pour la catégorie INTANGIBLES
    private String intangibleAsset; // Type d'actif incorporel (ex: Brevet, Licence, etc.)
    private Double intangibleValue; // Valeur de l'actif incorporel
    private Date rightsExpiryDate; // Date d'expiration des droits / licence
    private String description; // Description de l'actif incorporel
    private String ownershipStatus; // Statut de propriété (ex: "Owner", "Licensor", "Licensee")
    private Double marketValue; // Valeur marchande de l'actif
    private Double investmentAmount; // Montant investi dans l'actif
    private Double expectedRevenue; // Revenu attendu de l'actif



    // Attributs spécifiques pour la catégorie OBLIGATIONS
    private Double bondAmount; // Montant de l'obligation
    private Double interestRate; // Taux d'intérêt de l'obligation
    private Integer maturityYears; // Date d'échéance de l'obligation
    private String issuer; // Émetteur de l'obligation (ex: "Government", "Corporation", etc.)
    private Double faceValue; // Valeur nominale de l'obligation

    // Méthode pour traiter la transaction, en tenant compte des catégories
    public void processTransaction() {
        try {
            if (this.challenge != null) {
                if (this.challenge.getCategory() == ChallengeCategory.CRYPTOCURRENCY) {
                    System.out.println("Processing a cryptocurrency transaction...");
                    this.price *= 1.02; // Hypothetical market fluctuation adjustment
                } else if (this.challenge.getCategory() == ChallengeCategory.INTANGIBLES) {
                    System.out.println("Processing an intangible asset transaction...");
                    // Ajustements spécifiques aux actifs incorporels
                    if (this.intangibleValue != null) {
                        this.price += this.intangibleValue; // Ajouter la valeur de l'actif incorporel à la transaction
                    }
                    if (this.marketValue != null) {
                        this.price += this.marketValue; // Ajouter la valeur marchande
                    }
                    // Par exemple, ajout d'une taxe ou d'une redevance
                    double licensingFee = 100.0; // Exemple de frais de licence
                    this.price += licensingFee;
                } else if (this.challenge.getCategory() == ChallengeCategory.OBLIGATIONS) {
                    System.out.println("Processing an obligation transaction...");
                    // Ajustements spécifiques aux obligations
                    if (this.bondAmount != null) {
                        this.price += this.bondAmount; // Ajouter le montant de l'obligation à la transaction
                    }
                    if (this.interestRate != null) {
                        double interest = this.bondAmount * (this.interestRate / 100); // Calcul des intérêts
                        this.price += interest; // Ajouter les intérêts à la transaction
                    }
                    if (this.faceValue != null) {
                        this.price += this.faceValue; // Ajouter la valeur nominale de l'obligation
                    }
                }
                this.status = Status.COMPLETED;
            } else {
                throw new IllegalArgumentException("No challenge associated with this transaction.");
            }
        } catch (Exception e) {
            this.status = Status.FAILED; // Met à jour le statut en cas d'erreur
            System.err.println("Error processing transaction: " + e.getMessage());
        }
    }


}
