package com.example.eventhub.auth.oauth;

import com.example.eventhub.enums.UserRole;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2=super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        String name = oAuth2.getAttribute("name");
        String email = oAuth2.getAttribute("email");
        String providerId=String.valueOf(oAuth2.getAttribute("id"));

        userRepository.findByProviderAndProviderId(provider,providerId)
                .map(user -> {
                    user.setName(name);
                    user.setEmail(email);
                    return userRepository.save(user);
                })
                .orElseGet(()-> userRepository.save(new User(name,email,provider,providerId, UserRole.USER)));

        return oAuth2;
    }
}
