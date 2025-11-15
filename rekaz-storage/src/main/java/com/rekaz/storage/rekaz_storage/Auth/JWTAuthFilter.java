package com.rekaz.storage.rekaz_storage.Auth;

import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import com.rekaz.storage.rekaz_storage.Exception.UnauthorizedException;
import com.rekaz.storage.rekaz_storage.Registry.StorageRegistry;

@Component
public class JWTAuthFilter extends OncePerRequestFilter{
    
    private final JWTService jwtService;
    private final StorageRegistry storageRegistry;

       public JWTAuthFilter(JWTService jwtService, StorageRegistry storageRegistry) {
           this.jwtService = jwtService;
           this.storageRegistry = storageRegistry;
       }

        @Override
        protected void doFilterInternal(
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain)
                throws ServletException, IOException {

            String requestPath = request.getRequestURI();
            if (requestPath.startsWith("/api/v1/storage")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized: you are Unauthorized to access this resource f");
            }

            try {
                
                final String token = authHeader.substring(7);

                final String storageType = jwtService.extractStorageType(token);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (storageType != null && authentication == null) { 

                    if (jwtService.isTokenValid(token, storageRegistry)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                storageRegistry,
                                storageRegistry.getStorageTpye(),
                                null
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken); 
                    }
                }

                filterChain.doFilter(request, response);
            } catch (Exception exception) {
              throw new IllegalAccessError(exception.getMessage());
            }
      }
}
