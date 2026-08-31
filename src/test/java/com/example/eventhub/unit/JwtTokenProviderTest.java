package com.example.eventhub.unit;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.jwt.JwtTokenProvider;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.user.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

public class JwtTokenProviderTest {

    JwtTokenProvider jwtTokenProvider=new JwtTokenProvider();
    @org.junit.jupiter.api.Test
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 900000L); // 15 минут
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L); // 7 дней
    }

    @Nested
    @DisplayName("Тестирование метода создания Access токена")
    class GenerateAccessTokenTest{
        @Test
        void generateAccessToken_ContainsCorrectUsername_ReturnsTrue(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal = new UserPrincipal(user);
            String accessToken=jwtTokenProvider.generateAccessToken(principal);

            assertThat(jwtTokenProvider.getUsername(accessToken)).isEqualTo("user@gmail.com");
        }
    }

    @Nested
    @DisplayName("Тестирование метода проверки истечения токена")
    class IsExpiredTest{
        @Test
        void isExpired_FreshToken_ReturnsFalse(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal = new UserPrincipal(user);
            String accessToken=jwtTokenProvider.generateAccessToken(principal);

            assertThat(jwtTokenProvider.isExpired(accessToken)).isFalse();
        }
        @Test
        void isExpired_OldToken_ReturnsTrue(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            String accessToken= Jwts.builder().subject(user.getEmail()).issuedAt(new Date(System.currentTimeMillis()-100000))
                    .expiration(new Date(System.currentTimeMillis()-50000)).signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970")))
                    .compact();

            assertThat(jwtTokenProvider.isExpired(accessToken)).isTrue();
        }
    }
    @Nested
    @DisplayName("Тестирование метода проверки валидации токена")
    class IsTokenValid{
        @Test
        void isTokenValid_ExpiredToken_ThrowsJwtException(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal = new UserPrincipal(user);
            String accessToken= Jwts.builder().subject(user.getEmail()).issuedAt(new Date(System.currentTimeMillis()-100000))
                    .expiration(new Date(System.currentTimeMillis()-50000)).signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970")))
                    .compact();

            assertThat(jwtTokenProvider.isTokenValid(accessToken,principal)).isFalse();
        }
        @Test
        void isTokenValid_DifferentUser_ReturnsFalse(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            User anotherUser = new User("anotherUser","anotheruser@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal = new UserPrincipal(user);
            UserPrincipal anotherPrincipal = new UserPrincipal(anotherUser);
            String accessToken= jwtTokenProvider.generateAccessToken(principal);

            assertThat(jwtTokenProvider.isTokenValid(accessToken,anotherPrincipal)).isFalse();
        }
        @Test
        void isTokenValid_MalformedToken_ThrowsJwtException(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal = new UserPrincipal(user);
            assertThatThrownBy(()->jwtTokenProvider.isTokenValid("malformed.token",principal)).isInstanceOf(JwtException.class).hasMessage("Invalid token");
        }
        @Test
        void isTokenValid_NullToken_ThrowsJwtException(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal = new UserPrincipal(user);
            assertThatThrownBy(()->jwtTokenProvider.isTokenValid(null,principal)).isInstanceOf(JwtException.class).hasMessage("The token is empty or null");
        }
        @Test
        void isTokenValid_MatchingUser_ReturnsTrue(){
            User user = new User("user","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal = new UserPrincipal(user);
            String accessToken= jwtTokenProvider.generateAccessToken(principal);

            assertThat(jwtTokenProvider.isTokenValid(accessToken,principal)).isTrue();
        }

    }
}
