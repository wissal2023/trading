package tn.esprit.similator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Portfolio implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Date dateCreated;
    Double totVal;
    Double accVal =100000.000;
    Double buyPow=100000.000;
    Double cash=100000.000;
    Double tdyChange;  // Gains/losses as a result of today's market activity
    Double annReturn;  // Percentage return extrapolated for a year
    Double totGainLoss; // Total gain/loss percentage

    @OneToOne(mappedBy = "portfolio", cascade = CascadeType.ALL)
    User user;
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    List<PlacingOrder> placingOrders= new ArrayList<>();
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    List<Holding> holdings= new ArrayList<>();
}

