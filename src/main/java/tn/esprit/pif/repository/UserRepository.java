package tn.esprit.pif.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pif.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}