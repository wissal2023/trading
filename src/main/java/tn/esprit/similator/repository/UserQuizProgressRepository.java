package tn.esprit.similator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.similator.entity.Quiz;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.entity.UserQuizProgress;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuizProgressRepository extends JpaRepository<UserQuizProgress, Long> {
  List<UserQuizProgress> findByUser(User user);
  Optional<UserQuizProgress> findByUserAndQuiz(User user, Quiz quiz);
}
