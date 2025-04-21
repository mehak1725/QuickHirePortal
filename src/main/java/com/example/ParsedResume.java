package com.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to store parsed resume data
 */
public class ParsedResume {
    private int id;
    private String candidateId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<String> education;
    private List<String> workExperience;
    private List<String> skills;
    private String status; // "pending", "shortlisted", "rejected"
    private String resumeText; // Original text content of the resume

    public ParsedResume() {
        this.education = new ArrayList<>();
        this.workExperience = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.status = "pending";
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getEducation() {
        return education;
    }

    public void setEducation(List<String> education) {
        this.education = education;
    }

    public void addEducation(String educationItem) {
        this.education.add(educationItem);
    }

    public List<String> getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(List<String> workExperience) {
        this.workExperience = workExperience;
    }

    public void addWorkExperience(String workExperienceItem) {
        this.workExperience.add(workExperienceItem);
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public void addSkill(String skill) {
        this.skills.add(skill);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    @Override
    public String toString() {
        return "ParsedResume{" +
                "id=" + id +
                ", candidateId='" + candidateId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", education=" + education +
                ", workExperience=" + workExperience +
                ", skills=" + skills +
                ", status='" + status + '\'' +
                '}';
    }
}
