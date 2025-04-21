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
        
        // Get login credentials
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Validate input
        if (email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            sendError(response, "Missing required fields");
            return;
        }
        
        // Validate recruiter credentials
        String recruiterId = DatabaseUtil.validateRecruiterLogin(email, password);
        
        if (recruiterId != null) {
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("recruiterId", recruiterId);
            session.setAttribute("userType", "recruiter");
            
            // Send success response
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Login successful");
            jsonResponse.put("recruiterId", recruiterId);
            
            response.getWriter().write(jsonResponse.toString());
        } else {
            sendError(response, "Invalid email or password");
        }
    }
    
    private void sendError(HttpServletResponse response, String message) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(jsonResponse.toString());
    }
}
