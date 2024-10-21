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
    String duration;// daily or untill cancelled
    @Enumerated(EnumType.STRING)
    tradeType tradeType; // Stocks,OPTIONS,Bonds,Commodities,Forex,Mutual_Funds,ETF
    @Enumerated(EnumType.STRING)
    orderType orderType; // Market, Limit, Stop_Limit, Trailing_Stop
    @Enumerated(EnumType.STRING)
    transacType transacType; // BUY, SELL, SHORT, COVER , call, put
    @Enumerated(EnumType.STRING)
    status status;// OPEN, FILLED, CANCELLED
    Double stopLoss;         // Stop-loss value
    Double takeProfit;       // Take-profit value
    Double leverage;         // Leverage or margin (optional)

    // Specific Fields for Bonds
    Double faceValue;        // Bonds: Face value of the bond
    Double couponRate;       // Bonds: Interest rate
    LocalDateTime maturityDate; // Bonds: Maturity date of the bond

    // Specific Fields for Options
    Double strikePrice;      // Options: Strike price for calls/puts
    LocalDateTime expirationDate; // Options: Expiration date of the option

    // Specific Fields for Commodities
    Double contractSize;     // Commodities: Contract size (e.g., 100 barrels of oil)
    LocalDateTime expiryDate; // Commodities: Expiry date for futures contracts

    // Specific Fields for Mutual Funds and ETFs
    Double nav;              // Mutual Funds/ETFs: Net Asset Value (price per unit)


    @ManyToOne
    @JsonIgnore
    Portfolio portfolio;

    @OneToMany(mappedBy="placingOrder")
    List<Transaction> transactions= new ArrayList<>();

}
