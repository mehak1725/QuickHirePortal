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
 * Servlet handling candidate login
 */
@WebServlet("/api/candidate/login")
public class CandidateLoginServlet extends HttpServlet {
    
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
        
        // Validate credentials
        String candidateId = DatabaseUtil.validateCandidateLogin(email, password);
        
        if (candidateId != null) {
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("candidateId", candidateId);
            session.setAttribute("userType", "candidate");
            
            // Get candidate name for personalized greeting
            String candidateName = DatabaseUtil.getCandidateName(candidateId);
            
            // Check if candidate has already uploaded a resume
            boolean hasResume = DatabaseUtil.hasResume(candidateId);
            
            // Send success response
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Login successful");
            jsonResponse.put("candidateId", candidateId);
            jsonResponse.put("name", candidateName);
            jsonResponse.put("hasResume", hasResume);
            
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
