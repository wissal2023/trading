package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.service.IUserService;

import java.util.List;
import java.util.Map;

@Tag(name = "User class")
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userServ;

    @GetMapping("/get-all-users")
    public List<User> getAllUsers() {
        return userServ.retrieveAllUsers();
    }

    @GetMapping("/get-user/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userServ.retrieveUser(userId);
    }

    @PostMapping("/add-user-and-assign-portfolio")
    public User addUserAndAssignPortfolio(@RequestBody User user) {
        return userServ.addUserAndAssignPortfolio(user);
    }

    @PutMapping("/modify-user")
    public User modifyUser(@RequestBody User user) {
        return userServ.modifyUser(user);
    }

    @DeleteMapping("/remove-user/{userId}")
    public void removeUser(@PathVariable Long userId) {
        userServ.removeUser(userId);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            userServ.registerUser(user);
            return ResponseEntity.ok("Registration successful. Please check your email to confirm.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            userServ.loginUser(email, password);
            return ResponseEntity.ok("Login successful.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}