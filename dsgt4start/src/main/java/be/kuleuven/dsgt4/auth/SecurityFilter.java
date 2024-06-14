package be.kuleuven.dsgt4.auth;

import be.kuleuven.dsgt4.User;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import io.jsonwebtoken.JwtParser;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;

import java.util.*;

@Component
public class SecurityFilter extends OncePerRequestFilter {


    @Autowired
    Boolean isProduction;

    //@Autowired
    FirebaseApp firebaseApp;

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
            try{
                // Split the token into its three parts: header, payload, and signature
                String[] parts = token.split("\\.");

                byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
                String payloadString = new String(payloadBytes);
                ObjectMapper mapper = new ObjectMapper();
                User user = null;
                try {
                    Map<String, Object> payloadMap = mapper.readValue(payloadString, Map.class);

                    String email = (String) payloadMap.get("email");
                    String role = (String) payloadMap.get("role");
                    user = new User(email, role);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                FirebaseAuthentication authentication = new FirebaseAuthentication(user);
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(authentication);
            } catch (Exception e) {
                System.out.println("Error decoding JWT: " + e.getMessage());
            }
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