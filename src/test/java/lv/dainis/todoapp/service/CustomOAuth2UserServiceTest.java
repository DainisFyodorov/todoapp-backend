package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.UserRepository;
import lv.dainis.todoapp.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void newUserTest() {
        String email = "dainis@gmail.com";
        Map<String, Object> attributes = Map.of("email", email);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "email"
        );

        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest))
                .thenReturn(mockOAuth2User);

        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "USER")));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void existingUserTest() {
        String email = "dainis@gmail.com";
        Map<String, Object> attributes = Map.of("email", email);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "email"
        );

        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest))
                .thenReturn(mockOAuth2User);

        User existingUser = new User();
        existingUser.setUsername(email);

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(existingUser));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "USER")));

        verify(userRepository, never()).save(any(User.class));
    }
}
