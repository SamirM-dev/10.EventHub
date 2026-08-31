package com.example.eventhub.auth.jwt;

import com.example.eventhub.auth.details.CustomUserDetailsService;
import com.example.eventhub.auth.details.UserPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (SecurityContextHolder.getContext().getAuthentication()==null&&token!=null){
            try {
                //Я для тебя оставляю тут пометку,ты ведь будешь читать мой код. Напомни мне про неё ,чтобы я у тебя спросил кое что
                UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(jwtTokenProvider.getUsername(token));
                if (jwtTokenProvider.isTokenValid(token,principal)){
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities()));
                }
            }
            catch (JwtException e){
                log.error("Jwt validation error : {}",e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request,response);
    }

    private String extractToken(HttpServletRequest request){
        String fullTokenString = request.getHeader("Authorization");
        if (StringUtils.hasText(fullTokenString)&&fullTokenString.startsWith("Bearer ")){
            return fullTokenString.substring(7);
        }
        return null;
    }

}
