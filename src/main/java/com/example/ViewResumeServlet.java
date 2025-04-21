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
        
        // Get session
        HttpSession session = request.getSession(false);
        if (session == null) {
            sendError(response, "Not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String userType = (String) session.getAttribute("userType");
        ParsedResume resume = null;
        
        if ("candidate".equals(userType)) {
            // Candidate viewing their own resume
            String candidateId = (String) session.getAttribute("candidateId");
            if (candidateId == null) {
                sendError(response, "Not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
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
