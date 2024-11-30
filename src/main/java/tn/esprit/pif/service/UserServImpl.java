package tn.esprit.pif.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pif.entity.User;
import tn.esprit.pif.repository.UserRepository;


import java.util.List;

@Service
@AllArgsConstructor
public class UserServImpl implements IUserService{

    UserRepository userRepo;
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
