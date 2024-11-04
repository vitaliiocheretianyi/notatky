package com.itsvitaliio.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (!isMultipartContent(request)) {
                // For non-multipart requests, wrap the request for logging
                //System.out.println("is NOT multipart");
                // RequestWrapper wrappedRequest = new RequestWrapper(request);
                // filterChain.doFilter(wrappedRequest, response);
                filterChain.doFilter(request, response);
            } else {
                // For multipart requests, proceed without wrapping
                //System.out.println("IS MULTIPART");
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean isMultipartContent(HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart/");
    }
}
