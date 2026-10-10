package com.taskforge.auth;

import com.taskforge.auth.dto.AuthResponse;
import com.taskforge.auth.dto.LoginRequest;
import com.taskforge.auth.dto.MeResponse;
import com.taskforge.auth.dto.RegisterRequest;
import com.taskforge.security.JwtService;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
      UserRepository users,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest req) {
    String email = req.email().trim().toLowerCase();
    if (users.existsByEmail(email)) {
      throw new IllegalStateException("Email already registered");
    }
    users.save(new User(email, passwordEncoder.encode(req.password()), req.role(), Instant.now()));
    return new AuthResponse(jwtService.generateToken(email, req.role().name()), email, req.role());
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest req) {
    String email = req.email().trim().toLowerCase();
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, req.password()));
    User user = users.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found"));
    return new AuthResponse(jwtService.generateToken(email, user.getRole().name()), email, user.getRole());
  }

  @Transactional(readOnly = true)
  public MeResponse me(String email) {
    User user = users.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found"));
    return new MeResponse(user.getEmail(), user.getRole());
  }
}
