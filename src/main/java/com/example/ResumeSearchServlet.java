package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet for searching resumes
 */
@WebServlet("/api/resume/search")
public class ResumeSearchServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        // Check if user is a recruiter
        HttpSession session = request.getSession(false);
        if (session == null || !"recruiter".equals(session.getAttribute("userType"))) {
            sendError(response, "Unauthorized", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // Get search keyword
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = ""; // Empty keyword means get all resumes
        }
        
        // Search for resumes
        List<ParsedResume> matchingResumes;
        if (keyword.trim().isEmpty()) {
            matchingResumes = DatabaseUtil.getAllResumes();
        } else {
            matchingResumes = DatabaseUtil.searchResumes(keyword);
        }
        
        // Prepare response
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", true);
        
        JSONArray resumesArray = new JSONArray();
        for (ParsedResume resume : matchingResumes) {
            JSONObject resumeObj = new JSONObject();
            resumeObj.put("id", resume.getId());
            resumeObj.put("candidateId", resume.getCandidateId());
            resumeObj.put("fullName", resume.getFullName());
            resumeObj.put("email", resume.getEmail());
            resumeObj.put("phoneNumber", resume.getPhoneNumber());
            resumeObj.put("status", resume.getStatus());
            
            // Add skills array
            JSONArray skillsArray = new JSONArray();
            for (String skill : resume.getSkills()) {
                skillsArray.put(skill);
            }
            resumeObj.put("skills", skillsArray);
            
            // Check if the resume contains the keyword
            if (!keyword.trim().isEmpty()) {
                final String searchKeyword = keyword.toLowerCase();
                
                boolean keywordInSkills = resume.getSkills().stream()
                        .anyMatch(skill -> skill.toLowerCase().contains(searchKeyword));
                
                boolean keywordInExperience = resume.getWorkExperience().stream()
                        .anyMatch(exp -> exp.toLowerCase().contains(searchKeyword));
                
                boolean keywordInEducation = resume.getEducation().stream()
                        .anyMatch(edu -> edu.toLowerCase().contains(searchKeyword));
                
                boolean keywordInText = resume.getResumeText() != null && 
                        resume.getResumeText().toLowerCase().contains(searchKeyword);
                
                resumeObj.put("matchesKeyword", keywordInSkills || keywordInExperience || 
                        keywordInEducation || keywordInText);
                resumeObj.put("searchKeyword", keyword);
            }
            
            resumesArray.put(resumeObj);
        }
        
        jsonResponse.put("resumes", resumesArray);
        jsonResponse.put("count", matchingResumes.size());
        jsonResponse.put("keyword", keyword);
        
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
