package com.example.eventhub.auth.oauth;

import com.example.eventhub.enums.UserRole;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidc = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        return processOAuth2User(oidc,provider);
    }

    public OidcUser processOAuth2User(OidcUser oidc, String provider){
        String name = oidc.getFullName();
        String email = oidc.getEmail();
        String providerId = oidc.getSubject();

        User u=userRepository.findByProviderAndProviderId(provider,providerId).orElseGet(()->new User(name,email,provider,providerId, UserRole.USER));
        u.setName(name);
        u.setEmail(email);
        userRepository.save(u);
        return oidc;
    }

}
