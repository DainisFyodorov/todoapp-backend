package lv.dainis.todoapp.controller;

import jakarta.validation.Valid;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.responsemodel.UserInfoResponse;
import lv.dainis.todoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getUserInformation(Authentication auth) {
        return ResponseEntity.ok(userService.getUserInformation(auth));
    }
}
