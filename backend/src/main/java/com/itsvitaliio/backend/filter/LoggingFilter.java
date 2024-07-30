package com.itsvitaliio.backend.filter;

import com.itsvitaliio.backend.utilities.RequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (!isMultipartContent(request)) {
                // For non-multipart requests, wrap the request for logging
                RequestWrapper wrappedRequest = new RequestWrapper(request);
                filterChain.doFilter(wrappedRequest, response);
            } else {
                // For multipart requests, proceed without wrapping
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
