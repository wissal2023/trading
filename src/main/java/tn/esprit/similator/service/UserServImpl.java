package tn.esprit.similator.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.Portfolio;
import tn.esprit.similator.entity.User;
import tn.esprit.similator.repository.UserRepo;
import java.util.Date;
import java.util.Optional;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServImpl implements IUserService {

    @Autowired
    private UserRepo userRepo;


    public User addUserAndAssignPortfolio(User user) {
        Portfolio portfolio = new Portfolio();
        portfolio.setTotVal(100000.00); // or set it based on your logic
        portfolio.setDateCreated(new Date());
        user.setPortfolio(portfolio);

        return userRepo.save(user);
    }


    @Override
    public List<User> retrieveAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User retrieveUser(Long userId) {
        return userRepo.findById(userId).orElse(null);
    }

    
    public void removeUser(Long userId) {
        userRepo.deleteById(userId);
    }

    @Override
    public User modifyUser(User user) {
        return userRepo.save(user);
    }

    @Override
    public User registerUser(User user) throws Exception {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new Exception("Email already in use.");
        }
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new Exception("Username already taken.");
        }
        user.setPassword(user.getPassword()); // Just save the plain password or hash it yourself
        user.setEnabled(true);  // Activer automatiquement pour simplifier
        return userRepo.save(user);
    }

    @Override
    public User loginUser(String email, String password) throws Exception {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found.");
        }
        User user = userOpt.get();
        if (!password.equals(user.getPassword())) {  // Vérifie si le mot de passe correspond
            throw new Exception("Invalid password.");
        }
        if (!user.isEnabled()) {
            throw new Exception("User account is not activated.");
        }
        return user;  // Login réussi
    }
}

}

