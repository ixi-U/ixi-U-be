package com.ixi_U.user.repository;

import com.ixi_U.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

// neo4j와 통신하여 사용자 정보를 저장 및 조회하는 역할
public interface UserRepository extends Neo4jRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndProvider(String nickname, String provider);

    Optional<User> findByKakaoId(Long kakao_id);
}
