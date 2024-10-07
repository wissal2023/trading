package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

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
    Double totVal;
    Date dateCreated;
    Float accVal;
    Float buyPow;
    Float cash;




    @ManyToOne
    User user;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    List<Trade> assets;




}

