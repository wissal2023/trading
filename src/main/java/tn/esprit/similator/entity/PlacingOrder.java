package tn.esprit.similator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import java.util.List;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlacingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String symbol;
    Double qty;
    Float Price;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    LocalDateTime date;// sysdate
    String note;
    String param; //$ ou %
    String duration;// daily or untill cancelled
  
    @Enumerated(EnumType.STRING)
    assetsType assetsType; // Stocks,OPTIONS,Bonds,Commodities,Forex,Mutual_Funds,ETF
    @Enumerated(EnumType.STRING)
    orderType orderType; // Market, Limit, Stop_Limit, Trailing_Stop
    @Enumerated(EnumType.STRING)
    actionType actionType; // BUY, SELL, SHORT, COVER , call, put
    @Enumerated(EnumType.STRING)
    Status status;// OPEN, FILLED, CANCELLED
    Double stopLoss; // TRAILING_STOP
    Double trailingStopPrice; // TRAILING_STOP
    Double takeProfit;
    Double margin;
    Double faceValue;
    Double couponRate;
    LocalDateTime maturityDate;
    LocalDateTime expirationDate;
    Double contractSize;
    LocalDateTime expiryDate;
    Double nav;

    @OneToMany(mappedBy = "placingOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    List<Transaction> transactions;

}
