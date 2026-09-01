package com.example.eventhub.integrity;

import com.example.eventhub.auth.AuthController;
import com.example.eventhub.auth.AuthService;
import com.example.eventhub.auth.details.CustomUserDetailsService;
import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.dto.LoginRequest;
import com.example.eventhub.auth.dto.RefreshRequest;
import com.example.eventhub.auth.dto.RegisterRequest;
import com.example.eventhub.auth.dto.TokenResponse;
import com.example.eventhub.auth.jwt.JwtTokenProvider;
import com.example.eventhub.auth.oauth.ExchangeRequest;
import com.example.eventhub.config.TestSecurityConfig;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.user.User;
import com.example.eventhub.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {


    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper json;
    @MockitoBean
    AuthService authService;
    @MockitoBean
    CustomUserDetailsService userDetailsService;
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("Тестирование метода регистрации")
    class RegisterTest{
        @Test
        void register_ShortPassword_Returns400() throws Exception{
            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(new RegisterRequest("User","user@gmail.com","1234", UserRole.USER))))
                    .andExpect(status().isBadRequest());
        }
        @Test
        void register_ValidData_Returns201() throws Exception{
            when(authService.register(any())).thenReturn(new UserResponse(1L,"User","user@gmail.com",UserRole.USER, LocalDateTime.now()));

            mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new RegisterRequest("User","user@gmail.com","12345678910", UserRole.USER))))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Тестирование метода входа")
    class LoginTest{
        @Test
        void login_InvalidEmail_Returns400() throws Exception{
            LoginRequest request=new LoginRequest("usergmail.com","1234567890");
            mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isBadRequest());
        }
        @Test
        void login_ValidData_Returns200AndTokenResponse()throws Exception{
            LoginRequest request=new LoginRequest("user@gmail.com","1234567890");
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken("Access");
            tokenResponse.setRefreshToken("Refresh");
            when(authService.login(request)).thenReturn(tokenResponse);

            String result =mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(json.readValue(result, TokenResponse.class)).extracting(TokenResponse::getAccessToken,TokenResponse::getRefreshToken).containsExactly("Access","Refresh");

        }
    }

    @Nested
    @DisplayName("Тестирование метода вывода данных о текущем пользователе")
    class MeTest{
        @Test
        void me_NotAuthenticatedUser_Returns403() throws Exception{
            mockMvc.perform(get("/api/v1/me")).andExpect(status().isForbidden());
        }
        @Test
        void me_AuthenticatedUser_Returns200AndUserResponse()throws Exception{
            User user = new User("User","email","password",UserRole.USER);
            UserResponse response = new UserResponse(1L,user.getName(),user.getEmail(),user.getRole(),LocalDateTime.now());
            UserPrincipal principal = new UserPrincipal(user);
            Authentication authentication=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            when(authService.me(principal)).thenReturn(response);

            String result=mockMvc.perform(get("/api/v1/me").with(authentication(authentication))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(json.readValue(result, UserResponse.class)).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("Тестирование метода обновления токена")
    class RefreshTest{
        @Test
        void refresh_ValidData_Returns200AndTokenResponse()throws Exception{
            RefreshRequest request = new RefreshRequest("oldRefresh");
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken("newAccess");
            tokenResponse.setRefreshToken("newRefresh");
            when(authService.refresh(request)).thenReturn(tokenResponse);

            String result =mockMvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(json.readValue(result, TokenResponse.class)).extracting(TokenResponse::getAccessToken,TokenResponse::getRefreshToken).containsExactly("newAccess","newRefresh");

        }
    }

    @Nested
    @DisplayName("Тестирование метода обмена одноразового кода на токен")
    class ExchangeTest{
      @Test
       void exchange_ValidCode_Returns200AndTokenResponse() throws Exception{
          ExchangeRequest request = new ExchangeRequest("code");
          TokenResponse tokenResponse = new TokenResponse();
          tokenResponse.setAccessToken("Access");
          tokenResponse.setRefreshToken("Refresh");
          when(authService.exchange(request)).thenReturn(tokenResponse);

          String result =mockMvc.perform(post("/api/v1/auth/exchange").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                  .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
          assertThat(json.readValue(result, TokenResponse.class)).extracting(TokenResponse::getAccessToken,TokenResponse::getRefreshToken).containsExactly("Access","Refresh");

      }
    }

    @Nested
    @DisplayName("Тестирование метода выхода")
    class LogoutTest{
        @Test
        void logout_NotAuthenticatedUser_Returns403() throws Exception{
            RefreshRequest request =new RefreshRequest("refresh");
            mockMvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(request))).andExpect(status().isForbidden());
        }
        @Test
        void logout_AuthenticatedUser_Returns200()throws Exception{
            User user = new User("User","email","password",UserRole.USER);
            RefreshRequest request =new RefreshRequest("refresh");
            UserPrincipal principal = new UserPrincipal(user);
            Authentication authentication=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());

            mockMvc.perform(post("/api/v1/auth/logout").with(authentication(authentication))
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isOk());

        }
    }
}
