package com.example.eventhub.auth.jwt;

import com.example.eventhub.auth.details.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.access-token-experation-ms}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-token-experation-ms}")
    private Long refreshTokenExpiration;

    private Key getKey(){
        byte[] key= Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(key);
    }

    public String generateAccessToken(UserPrincipal principal){
        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+accessTokenExpiration))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(UserPrincipal principal){
        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+refreshTokenExpiration))
                .signWith(getKey())
                .compact();
    }

    public String getUsername(String token){
        return extractToken(token).getSubject();
    }

    public boolean isExpired(String token){
        try {
            return extractToken(token).getExpiration().before(new Date());
        }
        catch (ExpiredJwtException e){
            return true;
        }
    }

    public boolean isTokenValid(String token,UserPrincipal principal){
        try {
            return !isExpired(token)&&getUsername(token).equals(principal.getUsername());
        }
        catch (ExpiredJwtException e){
            throw new JwtException("The token has expired");
        }
        catch (UnsupportedJwtException e){
            throw new JwtException("Unsupported token format");
        }
        catch (MalformedJwtException e){
            throw new JwtException("Invalid token");
        }
        catch (SignatureException e){
            throw new JwtException("Invalid token signature");
        }
        catch (IllegalArgumentException e){
            throw new JwtException("The token is empty or null");
        }
    }

    public Claims extractToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
