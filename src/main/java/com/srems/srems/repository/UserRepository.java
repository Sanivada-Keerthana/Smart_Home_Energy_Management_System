package com.srems.srems.repository;

import com.srems.srems.model.Role;
import com.srems.srems.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /* ---------- BASIC LOOKUPS ---------- */

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    /* ---------- APPROVAL & STATUS ---------- */

    List<User> findByApprovedFalse();

    List<User> findByRoleAndApprovedFalse(Role role);

    /* ---------- FLAT / BLOCK LOGIC ---------- */

    // ✅ Check if a flat already has an owner
    boolean existsByFlat_FlatIdAndRole(Long flatId, Role role);

    // ✅ Get all users in a flat
    List<User> findByFlat_FlatId(Long flatId);

    // ✅ Get users in a flat by role
    List<User> findByFlat_FlatIdAndRole(Long flatId, Role role);

    // ✅ Get all users in a block
    List<User> findByBlockId(Long blockId);

    /* ---------- ROLE BASED ---------- */

    List<User> findByRole(Role role);

    long countByRole(Role role);

    
}