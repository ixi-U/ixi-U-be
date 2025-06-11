package com.ixi_U.user.service;

import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<Subscribed> getMySubscribedPlans() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        return user.getSubscribedHistory(); // Subscribed 안에 Plan 있음
    }
}
