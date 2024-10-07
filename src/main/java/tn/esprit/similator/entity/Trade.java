package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String symbol;
    Double price;
    Double quantity;
    String descp;
    Float px;
    Double qte;
    Date date;
    String param;
    Double duration;
    String status;
    Double commiss;
    Double dividende;
    String note;

    @Enumerated(EnumType.STRING)
    orderType orderType;
    @Enumerated(EnumType.STRING)
    tradeType tradeType;

    @ManyToOne
    Portfolio portfolio;





}

