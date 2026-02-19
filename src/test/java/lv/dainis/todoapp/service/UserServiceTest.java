package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.UserRepository;
import lv.dainis.todoapp.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @DisplayName("Find by username (success)")
    @Test
    void findByUsernameTest() {
        String username = "Dainis";

        User existingUser = new User();
        existingUser.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        assertEquals(existingUser, userService.findByUsername(username));
    }

    @DisplayName("Find by username (user not found)")
    @Test
    void findByUsernameUserNotFoundTest() {
        String username = "Dainis";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findByUsername(username));
    }

    @DisplayName("Register user (success)")
    @Test
    void registerUserTest() {
        String username = "Dainis";

        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword(rawPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        userService.registerUser(user);

        assertEquals(encodedPassword, user.getPassword(), "Password should be encoded");

        verify(userRepository, times(1)).save(user);
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @DisplayName("Register user (username is already taken)")
    @Test
    void registerUserWhenUsernameAlreadyTakenTest() {
        String username = "Dainis";

        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(user));

        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
