package tn.esprit.similator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

    @Column(nullable = false, unique = true)
     String username;

    @Column(nullable = false, unique = true)
     String email;

    @Column(nullable = false)
     String password;

     Double rank;

    @Column(nullable = false)
     boolean isEnabled = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    Portfolio portfolio;
}



}
