package com.example;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Enhanced Filter to add CORS headers to all responses and handle cross-origin communication
 */
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("CORS Filter initialized - Ready to handle cross-origin requests");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Get request origin and use it if available, otherwise use *
        String origin = httpRequest.getHeader("Origin");
        if (origin == null) {
            origin = "*";
        }
        
        // Add CORS headers with dynamic origin handling
        httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Candidate-ID, X-User-Type, Accept, Origin, X-Requested-With");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Add security headers for better protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Handle preflight OPTIONS requests with better response
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Add debug information for health check and connection troubleshooting
        if (httpRequest.getServletPath().equals("/healthcheck.html") || 
            httpRequest.getServletPath().equals("/api/healthcheck")) {
            System.out.println("---------- HEALTHCHECK REQUEST INFO ----------");
            System.out.println("Request URI: " + httpRequest.getRequestURI());
            System.out.println("Remote Address: " + httpRequest.getRemoteAddr());
            System.out.println("Request Method: " + httpRequest.getMethod());
            System.out.println("Request Headers:");
            
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = httpRequest.getHeader(headerName);
                System.out.println("  " + headerName + ": " + headerValue);
            }
            System.out.println("--------------------------------------------");
        }
        
        // Continue the request chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to release
    }
}