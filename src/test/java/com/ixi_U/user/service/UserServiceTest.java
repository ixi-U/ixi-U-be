package com.ixi_U.user.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ixi_U.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    @Test
    @DisplayName("유저가 존재 시 삭제되는지 확인")
    void deleteUserById() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.existsById("user1")).thenReturn(true);

        UserService service = new UserService(repo);
        service.deleteUserById("user1");

        verify(repo).deleteById("user1");
    }
}
