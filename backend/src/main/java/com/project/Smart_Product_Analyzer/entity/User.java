package com.project.Smart_Product_Analyzer.entity;

import jakarta.persistence.*;

/**
 * Entity representing a user in the database.
 */
@Entity
public class User {
    // Unique identifier for the user
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // Username for authentication
    @Column(unique = true, nullable = false)
    private String userName;
    // Encrypted password
    @Column(nullable = true)
    private String password;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
