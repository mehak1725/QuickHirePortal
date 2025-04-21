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
 * Servlet for updating candidate status (shortlist/reject)
 */
@WebServlet("/api/resume/status")
public class CandidateStatusServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        // Log request information for debugging
        System.out.println("CandidateStatusServlet: Received status update request");
        System.out.println("Content-Type: " + request.getContentType());
        
        // Alternative authentication methods for recruiters
        boolean isAuthenticated = false;
        
        // Method 1: Check session authentication
        HttpSession session = request.getSession(false);
        if (session != null && "recruiter".equals(session.getAttribute("userType"))) {
            System.out.println("Authenticated via session as recruiter");
            isAuthenticated = true;
        }
        
        // Method 2: Check header authentication
        if (!isAuthenticated && "recruiter".equals(request.getHeader("X-User-Type"))) {
            System.out.println("Authenticated via header as recruiter");
            isAuthenticated = true;
        }
        
        if (!isAuthenticated) {
            sendError(response, "Unauthorized", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Get resume ID and new status
        String resumeIdStr = request.getParameter("resumeId");
        String status = request.getParameter("status");
        
        System.out.println("Received parameters - resumeId: " + resumeIdStr + ", status: " + status);
        
        // Validate input
        if (resumeIdStr == null || resumeIdStr.trim().isEmpty()) {
            sendError(response, "Resume ID is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        if (status == null || !(status.equals("shortlisted") || status.equals("rejected") || status.equals("pending"))) {
            sendError(response, "Invalid status. Must be 'shortlisted', 'rejected', or 'pending'", 
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            int resumeId = Integer.parseInt(resumeIdStr);
            System.out.println("Updating resume " + resumeId + " status to " + status);
            
            // Update resume status
            boolean updated = DatabaseUtil.updateResumeStatus(resumeId, status);
            
            if (updated) {
                // Send success response
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("success", true);
                jsonResponse.put("message", "Resume status updated successfully");
                jsonResponse.put("resumeId", resumeId);
                jsonResponse.put("status", status);
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(jsonResponse.toString());
                System.out.println("Status updated successfully");
            } else {
                System.out.println("Database update failed for resume " + resumeId);
                sendError(response, "Failed to update resume status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid resume ID format: " + resumeIdStr);
            sendError(response, "Invalid resume ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            sendError(response, "Server error: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        
        response.setStatus(status);
        response.getWriter().write(jsonResponse.toString());
    }
}
