package com.example.eventhub.auth;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.dto.LoginRequest;
import com.example.eventhub.auth.dto.RefreshRequest;
import com.example.eventhub.auth.dto.RegisterRequest;
import com.example.eventhub.auth.dto.TokenResponse;
import com.example.eventhub.auth.jwt.JwtTokenProvider;
import com.example.eventhub.auth.jwt.RefreshToken;
import com.example.eventhub.auth.jwt.RefreshTokenRepository;
import com.example.eventhub.auth.oauth.ExchangeRequest;
import com.example.eventhub.exception.ResourceAlreadyExistsException;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.helper.OneTimeCodeStore;
import com.example.eventhub.user.User;
import com.example.eventhub.user.UserRepository;
import com.example.eventhub.user.dto.UserResponse;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final HelpForService helpForService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OneTimeCodeStore oneTimeCodeStore;

    public UserResponse register(RegisterRequest request){
       if ( userRepository.findByEmail(request.email()).isPresent()){
           throw new ResourceAlreadyExistsException("User with email: "+request.email()+" already exists");
       }

       return toResponse(userRepository.save(new User(request.name(),request.email(),encoder.encode(request.password()),request.role())));
    }

    public TokenResponse login(LoginRequest request){
        User user=userRepository.findByEmail(request.email()).orElseThrow(()->new EntityNotFoundException("User with email:"+request.email()+" does not exists"));
        if (user.getPassword()==null){
            throw new BadCredentialsException("This account was registered with " + user.getProvider());
        }
        Authentication authentication =authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken=jwtTokenProvider.generateAccessToken(principal);
        String refreshToken=jwtTokenProvider.generateRefreshToken(principal);
        refreshTokenRepository.save(new RefreshToken(refreshToken,principal.getUser(), LocalDateTime.now().plusDays(7)));
        return toResponseToken(accessToken,refreshToken);
    }

    public UserResponse me(UserPrincipal principal){
        return toResponse(principal.getUser());
    }

    public TokenResponse refresh(RefreshRequest request){
       RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken()).orElseThrow(()->new BadCredentialsException("Token does not exists"));

       if (jwtTokenProvider.isExpired(refreshToken.getToken())){
           refreshTokenRepository.delete(refreshToken);
           throw new JwtException("Token is expired");
       }

       UserPrincipal principal = new UserPrincipal(refreshToken.getUser());
       String access= jwtTokenProvider.generateAccessToken(principal);
       String refresh= jwtTokenProvider.generateRefreshToken(principal);

       refreshToken.setToken(refresh);
       refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
       refreshTokenRepository.save(refreshToken);

       return toResponseToken(access,refresh);
    }

    public TokenResponse exchange(ExchangeRequest request){
        Long userId=oneTimeCodeStore.consumeOAuth(request.code());
        if (userId==null){
            throw new BadCredentialsException("Code was expired");
        }
        UserPrincipal principal = new UserPrincipal(helpForService.idCheck(userId,userRepository,"User"));
        String access= jwtTokenProvider.generateAccessToken(principal);
        String refresh= jwtTokenProvider.generateRefreshToken(principal);

        refreshTokenRepository.save(new RefreshToken(refresh,principal.getUser(),LocalDateTime.now().plusDays(7)));

        return toResponseToken(access,refresh);
    }

    public void logout(RefreshRequest request){
        refreshTokenRepository.delete(refreshTokenRepository.findByToken(request.refreshToken()).orElseThrow(()->new JwtException("Token not found")));
    }

    public UserResponse toResponse(User user){
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),user.getCreatedAt());
    }
    public TokenResponse toResponseToken(String access,String refresh){
        TokenResponse response = new TokenResponse();
        response.setAccessToken(access);
        response.setRefreshToken(refresh);
        return response;
    }
}
