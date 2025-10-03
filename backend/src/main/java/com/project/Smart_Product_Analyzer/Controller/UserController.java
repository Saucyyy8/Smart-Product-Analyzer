package com.project.Smart_Product_Analyzer.Controller;


import com.project.Smart_Product_Analyzer.AuthService.UserService;
import com.project.Smart_Product_Analyzer.entity.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user registration and login endpoints.
 */
@RestController
public class UserController {

    // Service for user registration and authentication logic
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    /**
     * Registers a new user using the UserService.
     */
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        //return userRepository.save(user);
        return userService.register(user);
    }

    /**
     * Authenticates a user and returns a JWT token if successful.
     */
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.verify(user);
//        var u = userRepository.findByUserName(user.getUserName());
//        if(!Objects.isNull(u))
//            return "success";
//        return "failure";
    }

}
