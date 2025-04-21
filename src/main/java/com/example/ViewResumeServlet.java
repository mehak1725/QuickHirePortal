package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet for viewing resume data
 */
@WebServlet("/api/resume/view")
public class ViewResumeServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        // Get authentication from session or alternative methods
        HttpSession session = request.getSession(false);
        String userType = null;
        String candidateId = null;
        
        // First try to get from session
        if (session != null) {
            userType = (String) session.getAttribute("userType");
            candidateId = (String) session.getAttribute("candidateId");
            System.out.println("Found session auth: userType=" + userType + ", candidateId=" + candidateId);
        }
        
        // Try alternative auth methods if session auth is incomplete
        if (userType == null || candidateId == null) {
            // Try header-based auth
            String headerUserType = request.getHeader("X-User-Type");
            String headerCandidateId = request.getHeader("X-Candidate-ID");
            
            if (headerUserType != null && headerCandidateId != null) {
                userType = headerUserType;
                candidateId = headerCandidateId;
                System.out.println("Found header auth: userType=" + userType + ", candidateId=" + candidateId);
                
                // Create a session with these values for future requests
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("userType", userType);
                newSession.setAttribute("candidateId", candidateId);
            }
            // Try parameter-based auth
            else if (request.getParameter("userType") != null && request.getParameter("candidateId") != null) {
                userType = request.getParameter("userType");
                candidateId = request.getParameter("candidateId");
                System.out.println("Found parameter auth: userType=" + userType + ", candidateId=" + candidateId);
                
                // Create a session with these values for future requests
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("userType", userType);
                newSession.setAttribute("candidateId", candidateId);
            }
        }
        
        // If still not authenticated, return error
        if (userType == null || (userType.equals("candidate") && candidateId == null)) {
            System.out.println("No authentication found in request");
            sendError(response, "Not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        ParsedResume resume = null;
        
        if ("candidate".equals(userType)) {
            // Candidate viewing their own resume
            resume = DatabaseUtil.getResumeForCandidate(candidateId);
            if (resume == null) {
                sendError(response, "No resume found", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else if ("recruiter".equals(userType)) {
            // Recruiter viewing a specific resume
            String resumeId = request.getParameter("id");
            if (resumeId == null || resumeId.trim().isEmpty()) {
                sendError(response, "Resume ID is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            try {
                int id = Integer.parseInt(resumeId);
                resume = DatabaseUtil.getResumeById(id);
                if (resume == null) {
                    sendError(response, "Resume not found", HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } catch (NumberFormatException e) {
                sendError(response, "Invalid resume ID", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else {
            sendError(response, "Unauthorized", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Prepare response
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", true);
        
        JSONObject resumeData = new JSONObject();
        resumeData.put("id", resume.getId());
        resumeData.put("candidateId", resume.getCandidateId());
        resumeData.put("fullName", resume.getFullName());
        resumeData.put("email", resume.getEmail());
        resumeData.put("phoneNumber", resume.getPhoneNumber());
        resumeData.put("status", resume.getStatus());
        
        // Convert lists to JSON arrays
        JSONArray educationArray = new JSONArray();
        for (String edu : resume.getEducation()) {
            educationArray.put(edu);
        }
        resumeData.put("education", educationArray);
        
        JSONArray experienceArray = new JSONArray();
        for (String exp : resume.getWorkExperience()) {
            experienceArray.put(exp);
        }
        resumeData.put("workExperience", experienceArray);
        
        JSONArray skillsArray = new JSONArray();
        for (String skill : resume.getSkills()) {
            skillsArray.put(skill);
        }
        resumeData.put("skills", skillsArray);
        
        jsonResponse.put("resume", resumeData);
        
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
