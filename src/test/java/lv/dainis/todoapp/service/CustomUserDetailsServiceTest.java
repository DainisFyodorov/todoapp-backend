package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.UserRepository;
import lv.dainis.todoapp.entity.User;
import lv.dainis.todoapp.entity.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @DisplayName("Load user by username (success)")
    @Test
    void loadUserByUsernameSuccessTest() {
        Long userId = 2L;
        String username = "Dainis";

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword("encoded password");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(userId, userDetails.getId());
        assertEquals(username, userDetails.getUsername());
        assertEquals("encoded password", userDetails.getPassword());
        assertFalse(userDetails.getAuthorities().isEmpty());
    }

    @DisplayName("Load user by username (user not found)")
    @Test
    void loadUserByUsernameNotFoundTest() {
        String username = "Dainis";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> userDetailsService.loadUserByUsername(username));

        assertTrue(exception.getMessage().contains("User not found"));
    }
}
