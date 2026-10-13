
package com.swiftroute.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swiftroute.backend.model.User;
import com.swiftroute.backend.model.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Check uniqueness without expecting a single matching user.
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    // Lists safely handle duplicate records already in the database.
    List<User> findAllByEmailIgnoreCase(String email);

    List<User> findAllByPhone(String phone);

    // Keep these for compatibility with existing code that uses them.
    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    Optional<User> findByPhone(String phone);

    List<User> findAllByRoleAndRiderApprovedFalse(Role role);
}