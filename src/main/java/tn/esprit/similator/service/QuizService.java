package tn.esprit.similator.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.QuizRepository;
import tn.esprit.similator.repository.UserQuizProgressRepository;
import tn.esprit.similator.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizService {
  @Autowired
  private QuizRepository quizRepository;

  @Autowired
  private UserQuizProgressRepository progressRepository;

  @Autowired
  private UserRepo userRepository;

  public Quiz createQuiz(Quiz quiz) {
    return quizRepository.save(quiz);
  }

  public QuizResponse submitAnswer(Long userId, Long quizId, String answer) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    Quiz quiz = quizRepository.findById(quizId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

    boolean isCorrect = quiz.getCorrectAnswer().equalsIgnoreCase(answer.trim());

    UserQuizProgress progress = progressRepository
      .findByUserAndQuiz(user, quiz)
      .orElse(new UserQuizProgress(user, quiz));

    if (!progress.isCompleted() && isCorrect) {
      progress.setCompleted(true);
      progress.setCompletedAt(LocalDateTime.now());
      progress.setPointsEarned(quiz.getPointsValue());

      // Update user's bonus points
      user.setBonusPoints(user.getBonusPoints() + quiz.getPointsValue());
      userRepository.save(user);

      progressRepository.save(progress);
    }

    return new QuizResponse(
      isCorrect,
      quiz.getExplanation(),
      isCorrect ? quiz.getPointsValue() : 0,
      user.getBonusPoints()
    );
  }

  public List<QuizDTO> getAvailableQuizzes(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    List<Quiz> allQuizzes = quizRepository.findAll();
    List<UserQuizProgress> userProgress = progressRepository.findByUser(user);

    return allQuizzes.stream()
      .map(quiz -> {
        boolean completed = userProgress.stream()
          .anyMatch(progress ->
            progress.getQuiz().getId().equals(quiz.getId()) &&
              progress.isCompleted()
          );
        return new QuizDTO(quiz, completed);
      })
      .collect(Collectors.toList());
  }
}
