package tn.esprit.similator.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.sound.sampled.Port;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class PlacingOrder  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String symbol;
    Double qty;
    Double Price;
    LocalDateTime date;
    String note;
    String param; //$ ou %
    Double duration;// daily or untill cancelled
    @Enumerated(EnumType.STRING)
    tradeType tradeType; // Stocks,OPTIONS,Bonds,Commodities,Forex,Mutual_Funds,ETF
    @Enumerated(EnumType.STRING)
    orderType orderType; // Market, Limit, Stop_Limit, Trailing_Stop
    @Enumerated(EnumType.STRING)
    transacType transacType; // BUY, SELL, SHORT, COVER , call, put
    @Enumerated(EnumType.STRING)
    status status;// OPEN, FILLED, CANCELLED

    @ManyToOne
    @JsonIgnore
    Portfolio portfolio;

    @OneToMany(mappedBy="placingOrder")
    List<Transaction> transactions= new ArrayList<>();

}
