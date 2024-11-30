package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.ArrayList;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String fullname;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;


    private Double rank;
    private boolean accountLocked;
    private boolean enabled;
    Double commissionRate = 0.0015;
    private int bonusPoints;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Role> roles;

    @OneToOne(cascade = CascadeType.ALL)
    @ToString.Exclude
    Portfolio portfolio;

    @ToString.Exclude
    @ManyToMany(mappedBy = "users")
    private Set<Challenge> challenges;
    @OneToMany(mappedBy = "user")
    private List<BacktestResult> backtestResults;
    @OneToMany(mappedBy = "user")
    private List<UserQuizProgress> quizProgress;

    @Override
    public String getName() {
        return email;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()) )
                        .collect(Collectors.toList());
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public User(String username, String password) {
        this.fullname = username;
        this.password = password;
        this.backtestResults = new ArrayList<>();
  }


}

