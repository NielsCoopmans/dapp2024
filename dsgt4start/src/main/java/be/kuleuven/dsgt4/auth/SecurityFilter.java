package be.kuleuven.dsgt4.auth;

import be.kuleuven.dsgt4.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired
    Boolean isProduction;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // TODO: (level 1) decode Identity Token and assign correct email and role
        // TODO: (level 2) verify Identity Token
        String token = getTokenFromRequest(request);
        if (isProduction) {
            if (token != null && !token.isEmpty()) {
                try {
                    FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);

                    // Decode Identity Token and Assign Correct Email and Role
                    String email = decoded.getEmail();
                    String role = (String) decoded.getClaims().get("role");
                    // Create User object with email and role
                    var user = new User(email, role);
                    // Create FirebaseAuthentication object with user details
                    FirebaseAuthentication authentication = new FirebaseAuthentication(user);

                    // Set the authentication object in the SecurityContext
                    SecurityContext context = SecurityContextHolder.getContext();
                    context.setAuthentication(authentication);
                } catch (FirebaseAuthException e) {
                    // If token verification fails, return unauthorized error
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            } else {
                // No token found, return unauthorized error
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not found");
                return;
            }
        }
        else {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format");
            }

            // Base64 decode header and payload parts
            String header = new String(Base64.getDecoder().decode(parts[0]));
            String payload = new String(Base64.getDecoder().decode(parts[1]));

            // Parse JSON objects from header and payload
            Map<String, Object> headerMap = new ObjectMapper().readValue(header, Map.class);
            Map<String, Object> payloadMap = new ObjectMapper().readValue(payload, Map.class);

            // Extract email and role from payload (assuming claims structure)
            String email = (String) payloadMap.get("email");
            String role = (String) payloadMap.get("role");
            var user = new User(email, role);
            FirebaseAuthentication authentication = new FirebaseAuthentication(user);
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication);
        }


        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private static class FirebaseAuthentication implements Authentication {
        private final User user;

        FirebaseAuthentication(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            if (user.isManager()) {
                return List.of(new SimpleGrantedAuthority("manager"));
            } else {
                return new ArrayList<>();
            }
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public User getPrincipal() {
            return this.user;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {

        }
        @Override
        public String getName() {
            return null;
        }
    }
}