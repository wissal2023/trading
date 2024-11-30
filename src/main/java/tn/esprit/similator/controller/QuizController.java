package tn.esprit.similator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.UserRepo;
import tn.esprit.similator.service.QuizService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

  @Autowired
  private QuizService quizService;
  @Autowired
  private UserRepo userRepository;

  @PostMapping
  public Quiz createQuiz(@RequestBody Quiz quiz) {
    return quizService.createQuiz(quiz);
  }

  @GetMapping("/user/{userId}")
  public List<QuizDTO> getAvailableQuizzes(@PathVariable Long userId) {
    return quizService.getAvailableQuizzes(userId);
  }

  @PostMapping("/{quizId}/submit")
  public QuizResponse submitAnswer(
    @PathVariable Long quizId,
    @RequestParam Long userId,
    @RequestBody Map<String, String> request
  ) {
    return quizService.submitAnswer(userId, quizId, request.get("answer"));
  }
 /* @GetMapping("/user/{userId}/summary")
  public QuizSummaryDTO getUserQuizSummary(@PathVariable Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    List<UserQuizProgress> userProgress = progressRepository.findByUser(user);
    return new QuizSummaryDTO(userProgress);
  }*/
}

