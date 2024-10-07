package tn.esprit.similator.service;

import tn.esprit.similator.entity.User;

import java.util.List;

public interface IUserService {
    public List<User> retrieveAllUsers();
    public User retrieveUser(Long userId);
    public User addUser(User c);
    public void removeUser(Long userId);
    public User modifyUser(User user);
}
