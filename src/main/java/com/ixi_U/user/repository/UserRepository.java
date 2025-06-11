package com.ixi_U.user.repository;

import com.ixi_U.user.entity.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

}
