package com.springsecurity.repository;

import com.springsecurity.model.Role;
import com.springsecurity.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    @Query("select u from User u where u.email = :email")
    public User findByEmail(String email);
    public User findByResetPasswordToken(String token);

    @Query("update User u SET u.failedAttempt = :failAttempt WHERE u.email = :email")
    @Modifying
    public void updateFailedAttempts(int failAttempt, String email);

}
