package com.example.healthtracking.service;

import com.example.healthtracking.model.User;
import com.example.healthtracking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public void saveUser(User user) {
        userRepository.saveUser(user);
    }

}
