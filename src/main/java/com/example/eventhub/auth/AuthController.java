package com.example.eventhub.auth;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.dto.LoginRequest;
import com.example.eventhub.auth.dto.RefreshRequest;
import com.example.eventhub.auth.dto.RegisterRequest;
import com.example.eventhub.auth.dto.TokenResponse;
import com.example.eventhub.auth.oauth.ExchangeRequest;
import com.example.eventhub.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request){
        UserResponse created= authService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/users/{}"+created.id())).body(created);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(authService.me(principal));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request){
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/auth/exchange")
    public ResponseEntity<TokenResponse> exchange(@RequestBody ExchangeRequest request){
        return ResponseEntity.ok(authService.exchange(request));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshRequest request){
        authService.logout(request);
        return ResponseEntity.ok("You have successfully logged out");
    }

}
