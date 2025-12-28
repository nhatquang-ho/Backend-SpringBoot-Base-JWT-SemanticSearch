package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Default JPA methods
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsActiveTrue();

    // Custom JPQL queries
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByName(@Param("name") String name);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    // Custom native SQL queries
    @Query(value = "SELECT * FROM users u WHERE u.email ILIKE %:email% AND u.is_active = true", 
           nativeQuery = true)
    List<User> findActiveUsersByEmailContaining(@Param("email") String email);

    @Query(value = """
        SELECT u.*, COUNT(p.id) as product_count 
        FROM users u 
        LEFT JOIN products p ON u.id = p.created_by 
        WHERE u.is_active = true 
        GROUP BY u.id 
        ORDER BY product_count DESC
        """, nativeQuery = true)
    List<User> findActiveUsersWithProductCount();

    // Pageable queries
    @Query("SELECT u FROM User u WHERE u.isActive = :isActive ORDER BY u.createdAt DESC")
    Page<User> findUsersByActiveStatus(@Param("isActive") Boolean isActive, Pageable pageable);
}