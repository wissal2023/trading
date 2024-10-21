package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.Portfolio;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.repository.UserRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServImpl implements IUserService{

    UserRepo userRepo;


    public User addUserAndAssignPortfolio(User user) {
        Portfolio portfolio = new Portfolio();
        user.setPortfolio(portfolio);
        return userRepo.save(user);
    }

    public List<User> retrieveAllUsers() {
        return userRepo.findAll();
    }

    public User retrieveUser(Long userId) {
        return userRepo.findById(userId).get();
    }

    public void removeUser(Long userId) {
        userRepo.deleteById(userId);
    }

    public User modifyUser(User user) {
        return userRepo.save(user);
    }
}


/*
    public User addUser(User usr) {
        return userRepo.save(usr);
    }
 */