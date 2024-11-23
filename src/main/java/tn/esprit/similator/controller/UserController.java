package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.service.IUserService;

import java.util.List;

@Tag(name = "User class")
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/user")
public class UserController {

    IUserService userServ;
    
    @GetMapping("/Get-all-users")
    public List<User> getUsers() {
        return userServ.retrieveAllUsers();
    }
    
    @GetMapping("/Get-user/{user-id}")
    public User retrieveUser(@PathVariable("user-id") Long userId) {
        return userServ.retrieveUser(userId);
    }
    @PostMapping("/addAndAssignPortfolio")
    public ResponseEntity<User> addUserAndAssignPortfolio(@RequestBody User user) {
        User createdUser = userServ.addUserAndAssignPortfolio(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/modify-user")
    public User modifyUser(@RequestBody User usr) {
        return userServ.modifyUser(usr);
    }

    @DeleteMapping("/remove-user/{user-id}")
    public void removeUser(@PathVariable("user-id") Long userId) {
        userServ.removeUser(userId);
    }


}
