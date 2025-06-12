package com.ixi_U.user.repository;

import com.ixi_U.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface UserRepository extends Neo4jRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndProvider(String nickname, String provider);

    Optional<User> findByKakaoId(Long kakao_id);
}
