package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
}
