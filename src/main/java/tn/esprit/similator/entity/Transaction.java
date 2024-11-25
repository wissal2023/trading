package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String symbol;
    Double price;
    Double quantity;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    LocalDateTime date;// sysdate
    Double totalAmount; // (price * quantity)
    String descp;//describes the nature of the transaction
    Double commiss;
    Double dividende= 0.0;
    //String transactionType;
    @ManyToOne
    @JsonIgnore
    Portfolio portfolio;
    @ManyToOne
    PlacingOrder placingOrder;
}

