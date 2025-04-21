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
 * Simple servlet for verifying candidate authentication
 * This endpoint just checks if the candidate exists and is valid
 * without requiring them to have a resume already uploaded
 */
@WebServlet("/api/candidate/verify")
public class CandidateVerifyServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        // Get authentication from session or alternative methods
        HttpSession session = request.getSession(false);
        String userType = null;
        String candidateId = null;
        
        // First try to get auth from session
        if (session != null) {
            userType = (String) session.getAttribute("userType");
            candidateId = (String) session.getAttribute("candidateId");
            System.out.println("Verify: Found session auth: userType=" + userType + ", candidateId=" + candidateId);
        }
        
        // Try alternative auth methods if session auth is incomplete
        if (userType == null || candidateId == null) {
            // Try header-based auth
            String headerUserType = request.getHeader("X-User-Type");
            String headerCandidateId = request.getHeader("X-Candidate-ID");
            
            if (headerUserType != null && headerCandidateId != null) {
                userType = headerUserType;
                candidateId = headerCandidateId;
                System.out.println("Verify: Found header auth: userType=" + userType + ", candidateId=" + candidateId);
                
                // Create a session with these values for future requests
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("userType", userType);
                newSession.setAttribute("candidateId", candidateId);
            }
            // Try parameter-based auth
            else if (request.getParameter("userType") != null && request.getParameter("candidateId") != null) {
                userType = request.getParameter("userType");
                candidateId = request.getParameter("candidateId");
                System.out.println("Verify: Found parameter auth: userType=" + userType + ", candidateId=" + candidateId);
                
                // Create a session with these values for future requests
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("userType", userType);
                newSession.setAttribute("candidateId", candidateId);
            }
        }
        
        // If still not authenticated, return error
        if (userType == null || (userType.equals("candidate") && candidateId == null)) {
            System.out.println("Verify: No authentication found in request");
            sendError(response, "Not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Make sure this is a candidate request
        if (!"candidate".equals(userType)) {
            sendError(response, "Only candidates can use this endpoint", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // Verify the candidate exists in the database
        if (!DatabaseUtil.candidateExistsById(candidateId)) {
            sendError(response, "Candidate not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // All verification passed, return success
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", true);
        jsonResponse.put("message", "Candidate verified successfully");
        jsonResponse.put("candidateId", candidateId);
        
        response.getWriter().write(jsonResponse.toString());
    }
    
    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        
        response.setStatus(status);
        response.getWriter().write(jsonResponse.toString());
    }
}