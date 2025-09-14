package com.example.backend.repository;

import com.example.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Default JPA methods
    Optional<Role> findByName(String name);
    boolean existsByName(String name);

    // Custom queries
    @Query("SELECT r FROM Role r WHERE r.name IN :roleNames")
    Set<Role> findByNameIn(@Param("roleNames") Set<String> roleNames);

    @Query(value = "SELECT r.* FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId", 
           nativeQuery = true)
    Set<Role> findRolesByUserId(@Param("userId") Long userId);
}