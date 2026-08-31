package com.example.eventhub.unit;

import com.example.eventhub.auth.oauth.MyOAuth2UserService;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MyOAuth2UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    OAuth2User oAuth2User;
    @InjectMocks
    MyOAuth2UserService myOAuth2UserService;

    @Nested
    @DisplayName("Тестирование метода входа пользователя через OAuth без OpenId")
    class ProcessOAuth2UserTest{
        @Test
        void processOAuth2User_NewUser_SetsRoleUser(){
            String provider="Github";
            when(oAuth2User.getAttribute("name")).thenReturn("User");
            when(oAuth2User.getAttribute("email")).thenReturn("User@gmail.com");
            when(oAuth2User.getAttribute("id")).thenReturn(14L);
            when(userRepository.findByProviderAndProviderId(any(),any())).thenReturn(Optional.empty());
            ArgumentCaptor<User> captor=ArgumentCaptor.forClass(User.class);

            myOAuth2UserService.processOAuth2User(oAuth2User,provider);
            verify(userRepository).save(captor.capture());

            SoftAssertions.assertSoftly(soft->{
                soft.assertThat(captor.getValue().getName()).isEqualTo("User");
                soft.assertThat(captor.getValue().getEmail()).isEqualTo("User@gmail.com");
                soft.assertThat(captor.getValue().getProviderId()).isEqualTo("14");
                soft.assertThat(captor.getValue().getProvider()).isEqualTo(provider);
                soft.assertThat(captor.getValue().getRole()).isEqualTo(UserRole.USER);
            });

        }
        @Test
        void processOAuth2User_ExistsUser_UpdatesNameAndEmailButBoNotChangeRole(){
            String provider="Github";
            User user=new User("oldUser","oldUser@gmail.com",provider,"14",UserRole.ORGANIZER);
            when(oAuth2User.getAttribute("name")).thenReturn("newUser");
            when(oAuth2User.getAttribute("email")).thenReturn("newUser@gmail.com");
            when(oAuth2User.getAttribute("id")).thenReturn(14L);
            when(userRepository.findByProviderAndProviderId(any(),any())).thenReturn(Optional.of(user));
            ArgumentCaptor<User> captor=ArgumentCaptor.forClass(User.class);

            myOAuth2UserService.processOAuth2User(oAuth2User,provider);
            verify(userRepository).save(captor.capture());

            SoftAssertions.assertSoftly(soft->{
                soft.assertThat(captor.getValue().getName()).isEqualTo("newUser");
                soft.assertThat(captor.getValue().getEmail()).isEqualTo("newUser@gmail.com");
                soft.assertThat(captor.getValue().getProviderId()).isEqualTo("14");
                soft.assertThat(captor.getValue().getProvider()).isEqualTo(provider);
                soft.assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ORGANIZER);
            });
        }
    }

}
