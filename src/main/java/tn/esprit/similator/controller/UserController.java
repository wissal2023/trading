package tn.esprit.similator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.service.IUserService;

import java.util.List;

@Tag(name = "User class")
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    IUserService userServ;
    
    @GetMapping("/Get-all-users")
    public List<User> getUsers() {
        List<User> listUtsers = userServ.retrieveAllUsers();
        return listUtsers;
    }
    
    @GetMapping("/Get-user/{user-id}")
    public User retrieveUser(@PathVariable("user-id") Long userId) {
        User user = userServ.retrieveUser(userId);
        return user;
    }

    @PostMapping("/Add-User")
    public User addUser(@RequestBody User u) {
        User user = userServ.addUser(u);
        return user;
    }
    @PutMapping("/modify-user")
    public User modifyUser(@RequestBody User usr) {
        User user = userServ.modifyUser(usr);
        return user;
    }

    @DeleteMapping("/remove-user/{user-id}")
    public void removeUser(@PathVariable("user-id") Long userId) {
        userServ.removeUser(userId);
    }


}
