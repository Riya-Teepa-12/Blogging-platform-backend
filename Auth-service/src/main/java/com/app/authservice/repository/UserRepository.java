package com.app.authservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.authservice.entity.Role;
import com.app.authservice.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByRole(Role role);

    @Query("select u from User u where lower(u.username) like lower(concat('%', :keyword, '%'))")
    List<User> searchByUsername(@Param("keyword") String keyword);

    void deleteByUserId(Long userId);
}
