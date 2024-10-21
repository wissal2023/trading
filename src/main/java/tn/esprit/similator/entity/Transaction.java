package tn.esprit.similator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String symbol;
    Float price;
    Double quantity;
    Date date;
    Double totalAmount; // (price * quantity)
    String descp;//describes the nature of the transaction
    Double commiss;
    Double dividende;

    @ManyToOne
    @JsonIgnore
    PlacingOrder placingOrder;

}

