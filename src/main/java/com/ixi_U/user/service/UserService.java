package com.ixi_U.user.service;

import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

//    private final UserRepository userRepository;
//
//    public User findOrCreateUser(String nickname, String provider) {
//        return userRepository.findByNameAndProvider(nickname, provider)
//                .orElseGet(() -> {
//                    User user = User.of(nickname, null, provider, null);
//                    return userRepository.save(user);
//                });
//    }
}
