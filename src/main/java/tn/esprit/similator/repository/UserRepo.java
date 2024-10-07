package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

}
