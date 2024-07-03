package com.itsvitaliio.backend.filter;

import org.springframework.stereotype.Component;
import com.itsvitaliio.backend.utilities.RequestWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

@Component
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        RequestWrapper requestWrapper = new RequestWrapper(httpRequest);

        System.out.println("Incoming request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
        System.out.println("Headers:");
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + httpRequest.getHeader(headerName));
        }

        System.out.println("Body:");
        System.out.println(requestWrapper.getBody());

        // Continue with the next filter in the chain or the target resource
        chain.doFilter(requestWrapper, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
