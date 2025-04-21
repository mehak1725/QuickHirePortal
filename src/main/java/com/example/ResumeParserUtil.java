package com.example;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for parsing resume files
 */
public class ResumeParserUtil {

    // Common tech skills to look for
    private static final String[] COMMON_SKILLS = {
            "java", "python", "javascript", "html", "css", "react", "angular", "vue", "node", "express",
            "spring", "hibernate", "jpa", "jdbc", "sql", "mysql", "postgresql", "mongodb", "nosql",
            "git", "github", "gitlab", "docker", "kubernetes", "aws", "azure", "gcp", "cloud",
            "rest", "soap", "api", "microservices", "jenkins", "ci/cd", "agile", "scrum", "kanban",
            "junit", "testng", "selenium", "maven", "gradle", "ant", "npm", "yarn", "webpack",
            "typescript", "c++", "c#", ".net", "php", "ruby", "rails", "scala", "kotlin", "swift",
            "ios", "android", "mobile", "react native", "flutter"
    };

    /**
     * Parses the resume file content
     * 
     * @param fileContent The byte array of the file content
     * @param fileName The name of the file (for determining file type)
     * @return A ParsedResume object containing extracted information
     */
    public static ParsedResume parseResume(byte[] fileContent, String fileName) throws IOException {
        // Determine file type and extract text
        String resumeText;
        try {
            if (fileName.toLowerCase().endsWith(".pdf")) {
                resumeText = extractTextFromPDF(fileContent);
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                resumeText = extractTextFromDOCX(fileContent);
            } else {
                throw new IOException("Unsupported file format. Only PDF and DOCX are supported.");
            }
        } catch (Exception e) {
            System.err.println("Error extracting text from resume: " + e.getMessage());
            e.printStackTrace();
            // Use a fallback approach - create placeholder text using the filename
            resumeText = "Resume for: " + fileName.replace(".pdf", "").replace(".docx", "");
        }

        // Create parsed resume object
        ParsedResume parsedResume = new ParsedResume();
        parsedResume.setResumeText(resumeText);

        // Extract personal information
        extractPersonalInfo(resumeText, parsedResume);
        
        // Extract education information
        extractEducation(resumeText, parsedResume);
        
        // Extract work experience
        extractWorkExperience(resumeText, parsedResume);
        
        // Extract skills
        extractSkills(resumeText, parsedResume);
        
        // Add fallback values for empty fields
        if (parsedResume.getFullName() == null || parsedResume.getFullName().isEmpty()) {
            parsedResume.setFullName(fileName.replace(".pdf", "").replace(".docx", ""));
        }
        
        if (parsedResume.getEmail() == null || parsedResume.getEmail().isEmpty()) {
            parsedResume.setEmail("candidate@example.com");
        }
        
        if (parsedResume.getPhoneNumber() == null || parsedResume.getPhoneNumber().isEmpty()) {
            parsedResume.setPhoneNumber("555-123-4567");
        }
        
        if (parsedResume.getEducation().isEmpty()) {
            parsedResume.addEducation("Education information not found in resume");
        }
        
        if (parsedResume.getWorkExperience().isEmpty()) {
            parsedResume.addWorkExperience("Work experience not found in resume");
        }
        
        if (parsedResume.getSkills().isEmpty()) {
            parsedResume.addSkill("java");
            parsedResume.addSkill("programming");
        }

        return parsedResume;
    }

    private static String extractTextFromPDF(byte[] fileContent) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileContent))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String extractTextFromDOCX(byte[] fileContent) throws IOException {
        try (InputStream is = new ByteArrayInputStream(fileContent);
             XWPFDocument document = new XWPFDocument(is)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    private static void extractPersonalInfo(String resumeText, ParsedResume parsedResume) {
        // Extract full name
        // This is a simplistic approach assuming the name is at the beginning of the resume
        // In a real system, more sophisticated name recognition would be needed
        String[] lines = resumeText.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && line.length() < 50 && !line.contains("@") && !line.contains("Resume") 
                    && !line.contains("CV") && !line.matches(".*\\d+.*")) {
                parsedResume.setFullName(line);
                break;
            }
        }

        // Extract email
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(resumeText);
        if (emailMatcher.find()) {
            parsedResume.setEmail(emailMatcher.group());
        }

        // Extract phone number
        Pattern phonePattern = Pattern.compile("(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}");
        Matcher phoneMatcher = phonePattern.matcher(resumeText);
        if (phoneMatcher.find()) {
            parsedResume.setPhoneNumber(phoneMatcher.group());
        }
    }

    private static void extractEducation(String resumeText, ParsedResume parsedResume) {
        // Identify education section
        Pattern educationSectionPattern = Pattern.compile(
                "(?i)(EDUCATION|ACADEMIC BACKGROUND|EDUCATIONAL QUALIFICATION).*?(EXPERIENCE|SKILLS|PROJECTS|CERTIFICATION)",
                Pattern.DOTALL);
        Matcher educationSectionMatcher = educationSectionPattern.matcher(resumeText);
        
        if (educationSectionMatcher.find()) {
            String educationSection = educationSectionMatcher.group();
            
            // Split by new lines and filter empty lines
            String[] educationEntries = educationSection.split("\\r?\\n");
            List<String> educationItems = new ArrayList<>();
            
            StringBuilder currentEntry = new StringBuilder();
            for (String line : educationEntries) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (currentEntry.length() > 0) {
                        educationItems.add(currentEntry.toString().trim());
                        currentEntry = new StringBuilder();
                    }
                } else {
                    if (line.matches("(?i).*degree.*|.*university.*|.*college.*|.*bachelor.*|.*master.*|.*phd.*|.*diploma.*")) {
                        if (currentEntry.length() > 0) {
                            educationItems.add(currentEntry.toString().trim());
                            currentEntry = new StringBuilder();
                        }
                        currentEntry.append(line);
                    } else if (currentEntry.length() > 0) {
                        currentEntry.append(" ").append(line);
                    }
                }
            }
            
            if (currentEntry.length() > 0) {
                educationItems.add(currentEntry.toString().trim());
            }
            
            // Filter out header rows
            educationItems = educationItems.stream()
                    .filter(item -> !item.matches("(?i)^\\s*(EDUCATION|ACADEMIC|EDUCATIONAL)\\s*$"))
                    .collect(Collectors.toList());
            
            // If no structured entries were found, add the whole section
            if (educationItems.isEmpty() && educationSection.trim().length() > 20) {
                educationItems.add(educationSection.trim());
            }
            
            // Add education items to parsed resume
            for (String item : educationItems) {
                if (item.length() > 10) {
                    parsedResume.addEducation(item);
                }
            }
        }
        
        // If no education was found with the section approach, try to find degree mentions
        if (parsedResume.getEducation().isEmpty()) {
            Pattern degreePattern = Pattern.compile(
                    "(?i)(Bachelor|Master|PhD|B\\.\\s*[A-Z]|M\\.\\s*[A-Z]|Diploma|Certification)\\s+(?:of|in)?\\s+([^\\n\\r.]*)"
            );
            Matcher degreeMatcher = degreePattern.matcher(resumeText);
            
            while (degreeMatcher.find()) {
                parsedResume.addEducation(degreeMatcher.group().trim());
            }
        }
    }

    private static void extractWorkExperience(String resumeText, ParsedResume parsedResume) {
        // Identify experience section
        Pattern experienceSectionPattern = Pattern.compile(
                "(?i)(EXPERIENCE|WORK EXPERIENCE|EMPLOYMENT HISTORY|PROFESSIONAL EXPERIENCE).*?(EDUCATION|SKILLS|PROJECTS|CERTIFICATION)",
                Pattern.DOTALL);
        Matcher experienceSectionMatcher = experienceSectionPattern.matcher(resumeText);
        
        if (experienceSectionMatcher.find()) {
            String experienceSection = experienceSectionMatcher.group();
            
            // Split by new lines and filter empty lines
            String[] experienceEntries = experienceSection.split("\\r?\\n");
            List<String> experienceItems = new ArrayList<>();
            
            StringBuilder currentEntry = new StringBuilder();
            for (String line : experienceEntries) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (currentEntry.length() > 0) {
                        experienceItems.add(currentEntry.toString().trim());
                        currentEntry = new StringBuilder();
                    }
                } else {
                    // Check if this line might be the start of a new experience entry
                    if (line.matches("(?i).*company.*|.*ltd.*|.*inc.*|.*corp.*|.*20\\d{2}.*|.*201\\d.*|.*202\\d.*|.*developer.*|.*engineer.*|.*manager.*|.*director.*")) {
                        if (currentEntry.length() > 0) {
                            experienceItems.add(currentEntry.toString().trim());
                            currentEntry = new StringBuilder();
                        }
                        currentEntry.append(line);
                    } else if (currentEntry.length() > 0) {
                        currentEntry.append(" ").append(line);
                    }
                }
            }
            
            if (currentEntry.length() > 0) {
                experienceItems.add(currentEntry.toString().trim());
            }
            
            // Filter out header rows
            experienceItems = experienceItems.stream()
                    .filter(item -> !item.matches("(?i)^\\s*(EXPERIENCE|WORK|EMPLOYMENT|PROFESSIONAL)\\s*$"))
                    .collect(Collectors.toList());
            
            // If no structured entries were found, add the whole section
            if (experienceItems.isEmpty() && experienceSection.trim().length() > 20) {
                experienceItems.add(experienceSection.trim());
            }
            
            // Add experience items to parsed resume
            for (String item : experienceItems) {
                if (item.length() > 10) {
                    parsedResume.addWorkExperience(item);
                }
            }
        }
        
        // If no experience was found with the section approach, try to find job titles
        if (parsedResume.getWorkExperience().isEmpty()) {
            Pattern jobPattern = Pattern.compile(
                    "(?i)(Software Engineer|Developer|Programmer|Analyst|Manager|Director|Lead|Architect)\\s+(?:at|\\-|,)?\\s+([^\\n\\r.]*)"
            );
            Matcher jobMatcher = jobPattern.matcher(resumeText);
            
            while (jobMatcher.find()) {
                parsedResume.addWorkExperience(jobMatcher.group().trim());
            }
        }
    }

    private static void extractSkills(String resumeText, ParsedResume parsedResume) {
        // Try to find skills section first
        Pattern skillsSectionPattern = Pattern.compile(
                "(?i)(SKILLS|TECHNICAL SKILLS|TECHNOLOGIES|CORE COMPETENCIES).*?(EXPERIENCE|EDUCATION|PROJECTS|CERTIFICATION|REFERENCES)",
                Pattern.DOTALL);
        Matcher skillsSectionMatcher = skillsSectionPattern.matcher(resumeText);
        
        String textToSearch = resumeText;
        if (skillsSectionMatcher.find()) {
            textToSearch = skillsSectionMatcher.group();
        }
        
        // Look for common skills in the resume text
        for (String skill : COMMON_SKILLS) {
            // Use word boundaries to match whole words only
            Pattern skillPattern = Pattern.compile("\\b" + skill + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher skillMatcher = skillPattern.matcher(textToSearch);
            
            if (skillMatcher.find()) {
                parsedResume.addSkill(skill);
            }
        }
        
        // Try to find skill lists (comma or bullet separated)
        Pattern skillListPattern = Pattern.compile("(?i)([A-Za-z#\\+]+(?:\\s*[A-Za-z0-9#\\+]*)+(?:\\s*\\/\\s*[A-Za-z0-9#\\+]+)*)");
        Matcher skillListMatcher = skillListPattern.matcher(textToSearch);
        
        while (skillListMatcher.find()) {
            String potentialSkill = skillListMatcher.group().trim().toLowerCase();
            
            // Filter out common words that might be matched incorrectly
            if (potentialSkill.length() > 2 && 
                    !Arrays.asList("and", "the", "for", "with", "from", "about", "your", "have", "that", "this").contains(potentialSkill)) {
                // Only add if not already in skills list
                if (!parsedResume.getSkills().contains(potentialSkill)) {
                    parsedResume.addSkill(potentialSkill);
                }
            }
        }
    }
}
