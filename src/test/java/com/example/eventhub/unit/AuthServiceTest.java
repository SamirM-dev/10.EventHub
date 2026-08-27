package com.example.eventhub.unit;

import com.example.eventhub.AuthService;
import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.dto.LoginRequest;
import com.example.eventhub.auth.dto.RefreshRequest;
import com.example.eventhub.auth.dto.RegisterRequest;
import com.example.eventhub.auth.dto.TokenResponse;
import com.example.eventhub.auth.jwt.JwtTokenProvider;
import com.example.eventhub.auth.jwt.RefreshToken;
import com.example.eventhub.auth.jwt.RefreshTokenRepository;
import com.example.eventhub.auth.oauth.ExchangeRequest;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.exception.ResourceAlreadyExistsException;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.helper.OneTimeCodeStore;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import com.example.eventhub.user.dto.UserResponse;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import org.h2.security.auth.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder encoder;
    @Mock
    HelpForService helpForService;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    OneTimeCodeStore oneTimeCodeStore;

    @InjectMocks
    AuthService authService;

    @Nested
    @DisplayName("Тестирование метода регистрации")
    class RegisterTest{

        @Test
        void registerUser_ValidData_ReturnsUserResponse(){
            when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
            User user= new User("testuser","testuser@mail.com","12345678", UserRole.USER);
            user.setId(1L);
            when(userRepository.save(any())).thenReturn(user);

            assertThat(authService.register(new RegisterRequest("testuser","testuser@mail.com","12345678", UserRole.USER))).isInstanceOf(UserResponse.class);
        }

        @Test
        void registerUser_ExistsEmail_ThrowsException(){
            User user= new User("testuser","testuser@mail.com","12345678", UserRole.USER);
            user.setId(1L);
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

            assertThatThrownBy(
                    ()->authService.register(new RegisterRequest("testuser","testuser@mail.com","12345678", UserRole.USER))
            ).isInstanceOf(ResourceAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Тестирование метода логина")
    class LoginTest{

        @Test
        void login_ValidData_ReturnsTokenResponse(){
            User user= new User("testuser","testuser@mail.com","12345678", UserRole.USER);
            user.setId(1L);
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
            Authentication authentication = new UsernamePasswordAuthenticationToken(new UserPrincipal(user),null,null);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(jwtTokenProvider.generateAccessToken(any())).thenReturn("Access");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("Refresh");
            when(refreshTokenRepository.save(any())).thenReturn(null);

            assertThat(authService.login(new LoginRequest("email","password"))).isNotNull().isInstanceOf(TokenResponse.class);
        }
        @Test
        void login_UserNotExists_ThrowsException(){
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(()->authService.login(new LoginRequest("email","password"))).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void login_OAuthRegisteredUser_ThrowsException(){
            User user= new User("testuser","testuser@mail.com",null, UserRole.USER);
            user.setId(1L);
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

            assertThatThrownBy(()->authService.login(new LoginRequest("email","password"))).isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("Тестирование метода получения сведений о текущем пользователе")
    class MeTest{
        @Test
        void me_ValidData_ReturnsUserResponse(){
            User user= new User("testuser","testuser@mail.com","12345678", UserRole.USER);
            user.setId(1L);

            assertThat(authService.me(new UserPrincipal(user))).isInstanceOf(UserResponse.class);
        }
    }

    @Nested
    @DisplayName("Тестирование метода обновления токена")
    class RefreshTest{
        @Test
        void refresh_NonExistsToken_ThrowsException(){
            when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

            assertThatThrownBy(()->authService.refresh(new RefreshRequest("Token"))).isInstanceOf(BadCredentialsException.class);
        }
        @Test
        void refresh_ExpiredToken_ThrowsException(){
            RefreshToken refreshToken = new RefreshToken();
            when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshToken));
            when(jwtTokenProvider.isExpired(any())).thenReturn(true);

            assertThatThrownBy(()->authService.refresh(new RefreshRequest("Token"))).isInstanceOf(JwtException.class);
        }
        @Test
        void refresh_ValidData_ReturnTokenResponse(){
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(new User());
            when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshToken));
            when(jwtTokenProvider.isExpired(any())).thenReturn(false);
            when(jwtTokenProvider.generateAccessToken(any())).thenReturn("Access");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("Refresh");
            when(refreshTokenRepository.save(any())).thenReturn(null);

            assertThat(authService.refresh(new RefreshRequest("Token"))).isInstanceOf(TokenResponse.class);
        }
    }

    @Nested
    @DisplayName("Тестирование метода обмена одноразового кода на токен")
    class ExchangeTest{
        @Test
        void exchange_CodeExpired_ThrowsException(){
            when(oneTimeCodeStore.consumeOAuth(any())).thenReturn(null);

            assertThatThrownBy(()->authService.exchange(new ExchangeRequest("Code"))).isInstanceOf(BadCredentialsException.class);
        }
        @Test
        void exchange_ValidCode_ReturnsTokenResponse(){
            when(oneTimeCodeStore.consumeOAuth(any())).thenReturn(1L);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(new User());
            when(jwtTokenProvider.generateAccessToken(any())).thenReturn("Access");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("Refresh");
            when(refreshTokenRepository.save(any())).thenReturn(null);

            assertThat(authService.exchange(new ExchangeRequest("Code"))).isInstanceOf(TokenResponse.class);
        }
    }

    @Nested
    @DisplayName("Тестирование метода выхода")
    class LogoutTest{
        @Test
        void logout_TokenNotExists_ThrowsException(){
            when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

            assertThatThrownBy(()->authService.logout(new RefreshRequest("Token"))).isInstanceOf(JwtException.class);
        }
    }
}
