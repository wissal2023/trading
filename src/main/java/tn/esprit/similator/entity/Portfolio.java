package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    Date dateCreated;

    Double totVal;
    Double accVal =100000.000;
    Double buyPow=100000.000;
    Double cash=100000.000;
    Double tdyChange=0.0;  // Gains/losses as a result of today's market activity
    Double annReturn=0.0;  // Percentage return extrapolated for a year
    Double totGainLoss=0.0; // Total gain/loss percentage

    @OneToOne(mappedBy = "portfolio", cascade = CascadeType.ALL)
            @JsonIgnore
    User user;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<PlacingOrder> placingOrders= new ArrayList<>();
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Holding> holdings= new ArrayList<>();


}

