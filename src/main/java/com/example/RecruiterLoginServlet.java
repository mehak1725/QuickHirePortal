package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.json.JSONObject;

/**
 * Servlet handling recruiter login
 */
@WebServlet("/api/recruiter/login")
public class RecruiterLoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        // Log content type for debugging
        System.out.println("Content-Type: " + request.getContentType());
        
        // Get login credentials
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Log parameters for debugging
        System.out.println("Email parameter: " + (email != null ? "Present" : "Null"));
        System.out.println("Password parameter: " + (password != null ? "Present" : "Null"));
        
        // Validate input
        if (email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            System.out.println("Login failed: Missing required fields");
            sendError(response, "Missing required fields");
            return;
        }
        
        // Trim values for better validation
        email = email.trim();
        password = password.trim();
        
        // Validate recruiter credentials
        String recruiterId = DatabaseUtil.validateRecruiterLogin(email, password);
        
        if (recruiterId != null) {
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("recruiterId", recruiterId);
            session.setAttribute("userType", "recruiter");
            
            // Log success
            System.out.println("Login successful for email: " + email);
            
            // Send success response
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Login successful");
            jsonResponse.put("recruiterId", recruiterId);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(jsonResponse.toString());
        } else {
            // Log failure
            System.out.println("Login failed: Invalid credentials for email: " + email);
            sendError(response, "Invalid email or password", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    private void sendError(HttpServletResponse response, String message) throws IOException {
        sendError(response, message, HttpServletResponse.SC_BAD_REQUEST);
    }
    
    private void sendError(HttpServletResponse response, String message, int statusCode) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        
        response.setStatus(statusCode);
        response.getWriter().write(jsonResponse.toString());
    }
}
