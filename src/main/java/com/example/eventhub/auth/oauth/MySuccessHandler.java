package com.example.eventhub.auth.oauth;

import com.example.eventhub.helper.OneTimeCodeStore;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MySuccessHandler implements AuthenticationSuccessHandler {

    private final OneTimeCodeStore oneTimeCodeStore;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        User user = userRepository.findByProviderAndProviderId(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId(),extractProvideId(oAuth2AuthenticationToken.getPrincipal())).orElseThrow(EntityNotFoundException::new);

        String code = oneTimeCodeStore.generateOAuth(user.getId());
        response.sendRedirect("http://localhost:3000/oauth2/callback?code="+code);
    }

    private String extractProvideId(OAuth2User user){
        if (user instanceof OidcUser oidc){
            return oidc.getSubject();
        }
        return String.valueOf(user.getAttribute("id"));
    }
}
