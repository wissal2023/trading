package tn.esprit.pif.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String username;
    String email;
    String password;
    Double balance;
    Double rank;

    @ToString.Exclude
    @ManyToMany(mappedBy = "users")
    private Set<Challenge> challenges;

   /* @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Portfolio> portfolios;*/
}
