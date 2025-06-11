package com.ixi_U.user.service;

import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User changeName(String userId, String newName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // setter 없이 withName으로 새 객체 생성
        User updated = user.withName(newName);
        return userRepository.save(updated);
    }

}
