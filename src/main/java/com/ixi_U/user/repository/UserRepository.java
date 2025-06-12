package com.ixi_U.user.repository;

import com.ixi_U.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface UserRepository extends Neo4jRepository<User, String> {

}
