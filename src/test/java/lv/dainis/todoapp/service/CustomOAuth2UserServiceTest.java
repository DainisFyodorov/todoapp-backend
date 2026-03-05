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
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
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

        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId("google")
                .clientId("test-id")
                .authorizationUri("{baseUrl}")
                .tokenUri("{baseUrl}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}")
                .build();

        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest))
                .thenReturn(mockOAuth2User);

        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        assertEquals(email, result.getAttribute("oauth2_username"));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "USER")));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void newGitHubUserTest() {
        Integer githubId = 150;
        String login = "DainisFyodorov";
        Map<String, Object> attributes = Map.of("login", login, "id", githubId);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "id"
        );

        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId("github")
                .clientId("test-id")
                .authorizationUri("{baseUrl}")
                .tokenUri("{baseUrl}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}")
                .build();

        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest))
                .thenReturn(mockOAuth2User);

        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(userRepository.findByGithubId(githubId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals(githubId, result.getAttribute("id"));
        assertEquals(login, result.getAttribute("oauth2_username"));
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

        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId("google")
                .clientId("test-id")
                .authorizationUri("{baseUrl}")
                .tokenUri("{baseUrl}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}")
                .build();

        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest))
                .thenReturn(mockOAuth2User);

        User existingUser = new User();
        existingUser.setUsername(email);

        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(userRepository.findByUsername(email)).thenReturn(Optional.of(existingUser));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        assertEquals(email, result.getAttribute("oauth2_username"));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "USER")));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void existingGitHubUserTest() {
        Integer githubId = 150;
        String login = "DainisFyodorov";
        Map<String, Object> attributes = Map.of("login", login, "id", githubId);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "id"
        );

        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId("github")
                .clientId("test-id")
                .authorizationUri("{baseUrl}")
                .tokenUri("{baseUrl}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}")
                .build();

        User existingUser = new User();
        existingUser.setUsername(login);
        existingUser.setGithubId(githubId);

        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest))
                .thenReturn(mockOAuth2User);

        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(userRepository.findByGithubId(githubId)).thenReturn(Optional.of(existingUser));

        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals(githubId, result.getAttribute("id"));
        assertEquals(login, result.getAttribute("login"));
        assertEquals(login, result.getAttribute("oauth2_username"));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "USER")));

        verify(userRepository, never()).save(any(User.class));
    }
}
