package org.example.repository;

import org.example.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.dealer WHERE u.username = :username")
    Optional<UserAccount> findByUsernameWithDealer(@Param("username") String username);

    @Query("SELECT u FROM UserAccount u JOIN FETCH u.dealer WHERE u.email = :email")
    Optional<UserAccount> findByEmailWithDealer(@Param("email") String email);

    // Method cho admin để lấy tất cả user account với thông tin role và dealer
    @Query("SELECT u FROM UserAccount u " +
           "LEFT JOIN FETCH u.roleId " +
           "LEFT JOIN FETCH u.dealer " +
           "ORDER BY u.createdDate DESC")
    List<UserAccount> findAllUsersWithRoleAndDealer();

    // Method để search user account theo keyword
    @Query("SELECT u FROM UserAccount u " +
           "LEFT JOIN FETCH u.roleId " +
           "LEFT JOIN FETCH u.dealer " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(u.roleId.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR (u.dealer IS NOT NULL AND LOWER(u.dealer.dealerName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.createdDate DESC")
    List<UserAccount> searchUsersWithRoleAndDealer(@Param("keyword") String keyword);

    // Method để search user account theo role name
    @Query("SELECT u FROM UserAccount u " +
           "LEFT JOIN FETCH u.roleId " +
           "LEFT JOIN FETCH u.dealer " +
           "WHERE LOWER(u.roleId.roleName) = LOWER(:roleName) " +
           "ORDER BY u.createdDate DESC")
    List<UserAccount> findUsersByRoleName(@Param("roleName") String roleName);

    // Method để search user account theo dealer name
    @Query("SELECT u FROM UserAccount u " +
           "LEFT JOIN FETCH u.roleId " +
           "LEFT JOIN FETCH u.dealer " +
           "WHERE u.dealer IS NOT NULL AND LOWER(u.dealer.dealerName) = LOWER(:dealerName) " +
           "ORDER BY u.createdDate DESC")
    List<UserAccount> findUsersByDealerName(@Param("dealerName") String dealerName);

    // Method để search user account theo cả role name và dealer name
    @Query("SELECT u FROM UserAccount u " +
           "LEFT JOIN FETCH u.roleId " +
           "LEFT JOIN FETCH u.dealer " +
           "WHERE LOWER(u.roleId.roleName) = LOWER(:roleName) " +
           "  AND u.dealer IS NOT NULL AND LOWER(u.dealer.dealerName) = LOWER(:dealerName) " +
           "ORDER BY u.createdDate DESC")
    List<UserAccount> findUsersByRoleAndDealer(@Param("roleName") String roleName, @Param("dealerName") String dealerName);
}
