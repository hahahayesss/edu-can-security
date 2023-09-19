package io.retak.can.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.retak.can.configuration.JwtConfigurationProperties;
import io.retak.can.model.account.Account;
import io.retak.can.payload.request.SignInRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final JwtConfigurationProperties jwtConfigurationProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        String secret = Base64.getEncoder().encodeToString(
                jwtConfigurationProperties.getSecret().getBytes());
        secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String getToken(SignInRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
        } catch (DisabledException e) {
            throw new RuntimeException("Hesap kapatılmış");
        } catch (LockedException e) {
            throw new RuntimeException("Hesap kilitlenmiş");
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Username ya da password hatalı");
        } catch (Exception e) {
            throw new RuntimeException("Birşeyler yolunda gitmedi");
        }

        Account account = accountService.getByUsername(request.getUsername());
        return createToken(account);
    }

    public String createToken(Account account) {
        Claims claims = Jwts.claims();
        claims.setSubject(account.getId());
        claims.setId(UUID.randomUUID().toString()); // TODO: need to save redis
        claims.put("username", account.getUsername());
        claims.put("scope", String.join(" ", account.getRoles()));
        claims.put("type", jwtConfigurationProperties.getPrefix());

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtConfigurationProperties.getValidity());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody();
        Collection<? extends GrantedAuthority> scope =
                Arrays.stream(claims.get("scope", String.class).split(" "))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
        User principal = new User(claims.getSubject(), "?", scope);
        return new UsernamePasswordAuthenticationToken(principal, claims, scope);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ignored) {
            return false;
        }
    }
}
