package com.ixi_U.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.common.AbstractNeo4jContainer;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.user.service.UserService;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BaseEntityAuditingTest extends AbstractNeo4jContainer {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("엔티티 저장 시 createdAt, updatedAt이 세팅된다")
    void testCreatedAndUpdatedAtSetOnSave() {
        // given & when
        User savedUser = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));

        // then
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("엔티티 수정 시 updatedAt이 변경된다")
    void testUpdatedAtChangesOnUpdate() {
        // given
        User savedUser = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));
        Instant oldUpdatedAt = savedUser.getUpdatedAt();

        // when
        User updatedUser = userRepository.save(
                userService.changeName(savedUser.getId(), "new 홍길동"));

        // then
        assertThat(updatedUser.getUpdatedAt()).isAfter(oldUpdatedAt);
    }

}
