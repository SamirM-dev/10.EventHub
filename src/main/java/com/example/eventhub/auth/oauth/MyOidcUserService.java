package com.example.eventhub.auth.oauth;

import com.example.eventhub.enums.UserRole;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidc = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String name = oidc.getFullName();
        String email = oidc.getEmail();
        String providerId = oidc.getSubject();

        userRepository.findByProviderAndProviderId(provider,providerId)
                .map(user -> {
                    user.setName(name);
                    user.setEmail(email);
                    return userRepository.save(user);
                })
                .orElseGet(()-> userRepository.save(new User(name,email,provider,providerId, UserRole.USER)));

        return oidc;

    }

}
