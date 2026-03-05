package lv.dainis.todoapp.service;

import lv.dainis.todoapp.dao.UserRepository;
import lv.dainis.todoapp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.delegate = new DefaultOAuth2UserService();
    }

    public CustomOAuth2UserService(UserRepository userRepository, DefaultOAuth2UserService delegate) {
        this.userRepository = userRepository;
        this.delegate = delegate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        User user = null;

        if(registrationId.equalsIgnoreCase("google")) {
            String email = oAuth2User.getAttribute("email");

            user = userRepository.findByUsername(email).orElseGet(() -> {
                User userCreated = new User();
                userCreated.setUsername(email);
                userCreated.setPassword(String.valueOf(UUID.randomUUID()));

                return userRepository.save(userCreated);
            });
        } else if(registrationId.equalsIgnoreCase("github")) {

            String username = oAuth2User.getAttribute("login");
            Integer githubId = oAuth2User.getAttribute("id");
            user = userRepository.findByGithubId(githubId).orElseGet(() -> {
                User userCreated = new User();
                userCreated.setGithubId(githubId);
                userCreated.setUsername(username);
                userCreated.setPassword(String.valueOf(UUID.randomUUID()));

                return userRepository.save(userCreated);
            });
        }

        if(user == null) {
            throw new RuntimeException("Something went wrong");
        }

        attributes.put("oauth2_username", user.getUsername());
        //attributes.put("oauth2_userid", user.getId());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "oauth2_username"
        );
    }
}
