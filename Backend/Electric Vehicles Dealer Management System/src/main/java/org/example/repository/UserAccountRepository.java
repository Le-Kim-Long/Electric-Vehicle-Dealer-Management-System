package org.example.repository;

import org.example.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByUsername(String username);

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.dealer WHERE u.username = :username")
    Optional<UserAccount> findByUsernameWithDealer(@Param("username") String username);

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.dealer WHERE u.email = :email")
    Optional<UserAccount> findByEmailWithDealer(@Param("email") String email);
}
