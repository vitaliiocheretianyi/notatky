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
            System.out.println("LoggingFilter triggered for request: " + request.getRequestURI());
            if (!isMultipartContent(request)) {
                // System.out.println("Request is not multipart, wrapping request.");
                RequestWrapper wrappedRequest = new RequestWrapper(request);
                // System.out.println("Wrapped request body: " + wrappedRequest.getBody());
                filterChain.doFilter(wrappedRequest, response);
            } else {
                // System.out.println("Request is multipart, proceeding without wrapping.");
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            // System.out.println("Error in LoggingFilter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request.getContentType();
        // System.out.println("Request Content-Type: " + contentType);
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }
    
}
