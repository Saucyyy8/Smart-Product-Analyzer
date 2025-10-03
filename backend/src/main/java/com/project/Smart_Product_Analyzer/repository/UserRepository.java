package com.project.Smart_Product_Analyzer.repository;


import com.project.Smart_Product_Analyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom finder for username.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds a user by their username.
     */
    User findByUserName(String userName);
}
