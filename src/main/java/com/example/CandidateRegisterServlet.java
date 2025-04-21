package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONObject;

/**
 * Servlet handling candidate registration
 */
@WebServlet("/api/candidate/register")
public class CandidateRegisterServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        // Get registration data
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Validate input
        if (name == null || name.trim().isEmpty() || 
            email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            sendError(response, "Missing required fields");
            return;
        }
        
        // Check if email is already registered
        if (DatabaseUtil.candidateExists(email)) {
            sendError(response, "Email already registered");
            return;
        }
        
        // Generate unique ID for the candidate
        String candidateId = UUID.randomUUID().toString();
        
        // Register the candidate
        boolean success = DatabaseUtil.registerCandidate(candidateId, name, email, password);
        
        if (success) {
            // Send success response
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Registration successful");
            jsonResponse.put("candidateId", candidateId);
            
            response.getWriter().write(jsonResponse.toString());
        } else {
            sendError(response, "Registration failed");
        }
    }
    
    private void sendError(HttpServletResponse response, String message) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(jsonResponse.toString());
    }
}
