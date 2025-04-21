package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 * Servlet handling resume upload and parsing
 */
@WebServlet("/api/resume/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class ResumeUploadServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("candidateId") == null) {
            sendError(response, "Not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String candidateId = (String) session.getAttribute("candidateId");
        
        // Get the uploaded file from the request
        Part filePart = request.getPart("resume");
        if (filePart == null) {
            sendError(response, "No file uploaded", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Get the file name and check file type
        String fileName = filePart.getSubmittedFileName();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".pdf") || fileName.toLowerCase().endsWith(".docx"))) {
            sendError(response, "Invalid file format. Only PDF and DOCX are supported.", 
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Read file content
        byte[] fileContent;
        try (InputStream fileInputStream = filePart.getInputStream()) {
            fileContent = IOUtils.toByteArray(fileInputStream);
        }
        
        // Parse the resume
        ParsedResume parsedResume;
        try {
            parsedResume = ResumeParserUtil.parseResume(fileContent, fileName);
            parsedResume.setCandidateId(candidateId);
            
            // Save the parsed resume to the database
            int resumeId = DatabaseUtil.saveResume(parsedResume);
            if (resumeId > 0) {
                parsedResume.setId(resumeId);
                
                // Send success response with parsed data
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("success", true);
                jsonResponse.put("message", "Resume uploaded and parsed successfully");
                jsonResponse.put("resumeId", resumeId);
                
                JSONObject parsedData = new JSONObject();
                parsedData.put("fullName", parsedResume.getFullName());
                parsedData.put("email", parsedResume.getEmail());
                parsedData.put("phoneNumber", parsedResume.getPhoneNumber());
                parsedData.put("education", parsedResume.getEducation());
                parsedData.put("workExperience", parsedResume.getWorkExperience());
                parsedData.put("skills", parsedResume.getSkills());
                
                jsonResponse.put("parsedData", parsedData);
                
                response.getWriter().write(jsonResponse.toString());
            } else {
                sendError(response, "Failed to save parsed resume", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            sendError(response, "Error parsing resume: " + e.getMessage(), 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
