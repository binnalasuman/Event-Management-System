package com.suman.eventmanagement.repository;

import com.suman.eventmanagement.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Users, Long> {   // use Long for bigint
    Optional<Users> findByEmail(String email);
    // derived query
    boolean existsByEmail(String email);
}
