package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.repository.UserRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServImpl implements IUserService{

    UserRepo userRepo;
    @Override
    public List<User> retrieveAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User retrieveUser(Long userId) {
        return userRepo.findById(userId).get();
    }

    @Override
    public User addUser(User usr) {
        return userRepo.save(usr);
    }

    @Override
    public void removeUser(Long userId) {
        userRepo.deleteById(userId);
    }

    @Override
    public User modifyUser(User user) {
        return userRepo.save(user);
    }
}
