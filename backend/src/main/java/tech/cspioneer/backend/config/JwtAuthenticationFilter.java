package tech.cspioneer.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.cspioneer.backend.utils.JwtUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userUuid;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            Map<String, Object> payload = jwtUtils.decode(jwt);
            userUuid = (String) payload.get("uid");
            String identity = (String) payload.get("identity");

            // Validate token expiration
            Long exp = (Long) payload.get("exp");
            if (exp == null || exp * 1000 < System.currentTimeMillis()) { // exp is in seconds, System.currentTimeMillis() is in milliseconds
                // Token is expired, proceed without setting authentication
                filterChain.doFilter(request, response);
                return;
            }

            if (userUuid != null && identity != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Create authority list directly from the 'identity' claim in the token
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(identity);
                
                // Create authentication token without querying the database
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userUuid, // The principal can be the UUID string
                        null,
                        Collections.singletonList(authority)
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // If any error occurs during token parsing, clear the context
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
