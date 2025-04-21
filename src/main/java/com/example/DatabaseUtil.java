package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {
    private static final String JDBC_URL = "jdbc:sqlite:db/quickhire.db";
    private static boolean initialized = false;

    static {
        try {
            // Make sure db directory exists
            File dbDir = new File("db");
            if (!dbDir.exists()) {
                dbDir.mkdir();
            }
            
            // Create database file path
            File dbFile = new File("db/quickhire.db");
            
            // Print database location for debugging
            System.out.println("Database location: " + dbFile.getAbsolutePath());
            
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (Exception e) {
            System.err.println("Error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    private static void initializeDatabase() {
        if (initialized) {
            return;
        }
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create candidates table
            stmt.execute("CREATE TABLE IF NOT EXISTS candidates (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL" +
                    ")");
            
            // Create recruiters table
            stmt.execute("CREATE TABLE IF NOT EXISTS recruiters (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL" +
                    ")");
            
            // Create resumes table
            stmt.execute("CREATE TABLE IF NOT EXISTS resumes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "candidate_id TEXT, " +
                    "full_name TEXT, " +
                    "email TEXT, " +
                    "phone_number TEXT, " +
                    "resume_text TEXT, " +
                    "status TEXT DEFAULT 'pending', " +
                    "FOREIGN KEY (candidate_id) REFERENCES candidates(id)" +
                    ")");
            
            // Create resume_education table
            stmt.execute("CREATE TABLE IF NOT EXISTS resume_education (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resume_id INTEGER, " +
                    "education_item TEXT, " +
                    "FOREIGN KEY (resume_id) REFERENCES resumes(id)" +
                    ")");
            
            // Create resume_experience table
            stmt.execute("CREATE TABLE IF NOT EXISTS resume_experience (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resume_id INTEGER, " +
                    "experience_item TEXT, " +
                    "FOREIGN KEY (resume_id) REFERENCES resumes(id)" +
                    ")");
            
            // Create resume_skills table
            stmt.execute("CREATE TABLE IF NOT EXISTS resume_skills (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resume_id INTEGER, " +
                    "skill_item TEXT, " +
                    "FOREIGN KEY (resume_id) REFERENCES resumes(id)" +
                    ")");
            
            // Insert a default admin/recruiter if not exists
            try {
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM recruiters WHERE email = 'admin@quickhire.com'");
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO recruiters (id, name, email, password) VALUES " +
                            "('admin123', 'Admin User', 'admin@quickhire.com', 'admin123')");
                }
                rs.close();
                checkStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            initialized = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Candidate methods
    public static boolean registerCandidate(String id, String name, String email, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO candidates (id, name, email, password) VALUES (?, ?, ?, ?)")) {
            
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean candidateExists(String email) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM candidates WHERE email = ?")) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String validateCandidateLogin(String email, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id FROM candidates WHERE email = ? AND password = ?")) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCandidateName(String id) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT name FROM candidates WHERE id = ?")) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Recruiter methods
    public static String validateRecruiterLogin(String email, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id FROM recruiters WHERE email = ? AND password = ?")) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Resume methods
    public static int saveResume(ParsedResume resume) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // Save main resume data
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO resumes (candidate_id, full_name, email, phone_number, resume_text) " +
                            "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, resume.getCandidateId());
            pstmt.setString(2, resume.getFullName());
            pstmt.setString(3, resume.getEmail());
            pstmt.setString(4, resume.getPhoneNumber());
            pstmt.setString(5, resume.getResumeText());
            
            pstmt.executeUpdate();
            
            int resumeId;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    resumeId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating resume failed, no ID obtained.");
                }
            }
            
            // Save education items
            PreparedStatement educationStmt = conn.prepareStatement(
                    "INSERT INTO resume_education (resume_id, education_item) VALUES (?, ?)");
            
            for (String education : resume.getEducation()) {
                educationStmt.setInt(1, resumeId);
                educationStmt.setString(2, education);
                educationStmt.addBatch();
            }
            educationStmt.executeBatch();
            
            // Save experience items
            PreparedStatement experienceStmt = conn.prepareStatement(
                    "INSERT INTO resume_experience (resume_id, experience_item) VALUES (?, ?)");
            
            for (String experience : resume.getWorkExperience()) {
                experienceStmt.setInt(1, resumeId);
                experienceStmt.setString(2, experience);
                experienceStmt.addBatch();
            }
            experienceStmt.executeBatch();
            
            // Save skills
            PreparedStatement skillsStmt = conn.prepareStatement(
                    "INSERT INTO resume_skills (resume_id, skill_item) VALUES (?, ?)");
            
            for (String skill : resume.getSkills()) {
                skillsStmt.setInt(1, resumeId);
                skillsStmt.setString(2, skill);
                skillsStmt.addBatch();
            }
            skillsStmt.executeBatch();
            
            conn.commit();
            return resumeId;
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static ParsedResume getResumeForCandidate(String candidateId) {
        try (Connection conn = getConnection()) {
            // Get the main resume data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id, full_name, email, phone_number, resume_text, status " +
                            "FROM resumes WHERE candidate_id = ?")) {
                
                pstmt.setString(1, candidateId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ParsedResume resume = new ParsedResume();
                        resume.setId(rs.getInt("id"));
                        resume.setCandidateId(candidateId);
                        resume.setFullName(rs.getString("full_name"));
                        resume.setEmail(rs.getString("email"));
                        resume.setPhoneNumber(rs.getString("phone_number"));
                        resume.setResumeText(rs.getString("resume_text"));
                        resume.setStatus(rs.getString("status"));
                        
                        // Get education items
                        loadEducation(conn, resume);
                        
                        // Get experience items
                        loadExperience(conn, resume);
                        
                        // Get skills
                        loadSkills(conn, resume);
                        
                        return resume;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void loadEducation(Connection conn, ParsedResume resume) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT education_item FROM resume_education WHERE resume_id = ?")) {
            
            pstmt.setInt(1, resume.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resume.addEducation(rs.getString("education_item"));
                }
            }
        }
    }

    private static void loadExperience(Connection conn, ParsedResume resume) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT experience_item FROM resume_experience WHERE resume_id = ?")) {
            
            pstmt.setInt(1, resume.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resume.addWorkExperience(rs.getString("experience_item"));
                }
            }
        }
    }

    private static void loadSkills(Connection conn, ParsedResume resume) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT skill_item FROM resume_skills WHERE resume_id = ?")) {
            
            pstmt.setInt(1, resume.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resume.addSkill(rs.getString("skill_item"));
                }
            }
        }
    }

    public static List<ParsedResume> getAllResumes() {
        List<ParsedResume> resumes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id, candidate_id, full_name, email, phone_number, resume_text, status FROM resumes")) {
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ParsedResume resume = new ParsedResume();
                    resume.setId(rs.getInt("id"));
                    resume.setCandidateId(rs.getString("candidate_id"));
                    resume.setFullName(rs.getString("full_name"));
                    resume.setEmail(rs.getString("email"));
                    resume.setPhoneNumber(rs.getString("phone_number"));
                    resume.setResumeText(rs.getString("resume_text"));
                    resume.setStatus(rs.getString("status"));
                    
                    loadEducation(conn, resume);
                    loadExperience(conn, resume);
                    loadSkills(conn, resume);
                    
                    resumes.add(resume);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return resumes;
    }

    public static List<ParsedResume> searchResumes(String keyword) {
        List<ParsedResume> results = new ArrayList<>();
        String searchTerm = "%" + keyword.toLowerCase() + "%";
        
        try (Connection conn = getConnection()) {
            // Search in resume text, full name, education, experience, and skills
            String sql = "SELECT DISTINCT r.id FROM resumes r " +
                    "LEFT JOIN resume_education e ON r.id = e.resume_id " +
                    "LEFT JOIN resume_experience x ON r.id = x.resume_id " +
                    "LEFT JOIN resume_skills s ON r.id = s.resume_id " +
                    "WHERE LOWER(r.resume_text) LIKE ? " +
                    "OR LOWER(r.full_name) LIKE ? " +
                    "OR LOWER(e.education_item) LIKE ? " +
                    "OR LOWER(x.experience_item) LIKE ? " +
                    "OR LOWER(s.skill_item) LIKE ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, searchTerm);
                pstmt.setString(2, searchTerm);
                pstmt.setString(3, searchTerm);
                pstmt.setString(4, searchTerm);
                pstmt.setString(5, searchTerm);
                
                List<Integer> resumeIds = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        resumeIds.add(rs.getInt("id"));
                    }
                }
                
                // Get full resume data for each matching ID
                for (Integer id : resumeIds) {
                    try (PreparedStatement resumeStmt = conn.prepareStatement(
                            "SELECT id, candidate_id, full_name, email, phone_number, resume_text, status " +
                                    "FROM resumes WHERE id = ?")) {
                        
                        resumeStmt.setInt(1, id);
                        
                        try (ResultSet rs = resumeStmt.executeQuery()) {
                            if (rs.next()) {
                                ParsedResume resume = new ParsedResume();
                                resume.setId(rs.getInt("id"));
                                resume.setCandidateId(rs.getString("candidate_id"));
                                resume.setFullName(rs.getString("full_name"));
                                resume.setEmail(rs.getString("email"));
                                resume.setPhoneNumber(rs.getString("phone_number"));
                                resume.setResumeText(rs.getString("resume_text"));
                                resume.setStatus(rs.getString("status"));
                                
                                loadEducation(conn, resume);
                                loadExperience(conn, resume);
                                loadSkills(conn, resume);
                                
                                results.add(resume);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return results;
    }

    public static ParsedResume getResumeById(int resumeId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id, candidate_id, full_name, email, phone_number, resume_text, status " +
                             "FROM resumes WHERE id = ?")) {
            
            pstmt.setInt(1, resumeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ParsedResume resume = new ParsedResume();
                    resume.setId(rs.getInt("id"));
                    resume.setCandidateId(rs.getString("candidate_id"));
                    resume.setFullName(rs.getString("full_name"));
                    resume.setEmail(rs.getString("email"));
                    resume.setPhoneNumber(rs.getString("phone_number"));
                    resume.setResumeText(rs.getString("resume_text"));
                    resume.setStatus(rs.getString("status"));
                    
                    loadEducation(conn, resume);
                    loadExperience(conn, resume);
                    loadSkills(conn, resume);
                    
                    return resume;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateResumeStatus(int resumeId, String status) {
        System.out.println("DatabaseUtil: Updating resume status - ID: " + resumeId + ", Status: " + status);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE resumes SET status = ? WHERE id = ?")) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, resumeId);
            
            System.out.println("Executing SQL update: UPDATE resumes SET status = '" + status + "' WHERE id = " + resumeId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected by update: " + rowsAffected);
            
            // Check if the resume exists but wasn't updated (status already set)
            if (rowsAffected == 0) {
                try (PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM resumes WHERE id = ?")) {
                    checkStmt.setInt(1, resumeId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            // Resume exists but wasn't updated (maybe status already set)
                            System.out.println("Resume exists but status wasn't changed (may already be '" + status + "')");
                            return true;
                        } else {
                            System.out.println("Resume with ID " + resumeId + " not found");
                        }
                    }
                }
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("SQL Error updating resume status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error in updateResumeStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasResume(String candidateId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM resumes WHERE candidate_id = ?")) {
            
            pstmt.setString(1, candidateId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if a candidate exists in the database by ID
     * @param candidateId The candidate ID to check
     * @return true if the candidate exists, false otherwise
     */
    public static boolean candidateExistsById(String candidateId) {
        if (candidateId == null || candidateId.trim().isEmpty()) {
            return false;
        }
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM candidates WHERE id = ?")) {
            
            pstmt.setString(1, candidateId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if candidate exists by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateResume(ParsedResume resume) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            // Update main resume data
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE resumes SET full_name = ?, email = ?, phone_number = ?, resume_text = ? " +
                            "WHERE id = ?");
            
            pstmt.setString(1, resume.getFullName());
            pstmt.setString(2, resume.getEmail());
            pstmt.setString(3, resume.getPhoneNumber());
            pstmt.setString(4, resume.getResumeText());
            pstmt.setInt(5, resume.getId());
            
            pstmt.executeUpdate();
            
            // Delete existing education, experience, and skills
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM resume_education WHERE resume_id = ?")) {
                deleteStmt.setInt(1, resume.getId());
                deleteStmt.executeUpdate();
            }
            
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM resume_experience WHERE resume_id = ?")) {
                deleteStmt.setInt(1, resume.getId());
                deleteStmt.executeUpdate();
            }
            
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM resume_skills WHERE resume_id = ?")) {
                deleteStmt.setInt(1, resume.getId());
                deleteStmt.executeUpdate();
            }
            
            // Save new education items
            PreparedStatement educationStmt = conn.prepareStatement(
                    "INSERT INTO resume_education (resume_id, education_item) VALUES (?, ?)");
            
            for (String education : resume.getEducation()) {
                educationStmt.setInt(1, resume.getId());
                educationStmt.setString(2, education);
                educationStmt.addBatch();
            }
            educationStmt.executeBatch();
            
            // Save new experience items
            PreparedStatement experienceStmt = conn.prepareStatement(
                    "INSERT INTO resume_experience (resume_id, experience_item) VALUES (?, ?)");
            
            for (String experience : resume.getWorkExperience()) {
                experienceStmt.setInt(1, resume.getId());
                experienceStmt.setString(2, experience);
                experienceStmt.addBatch();
            }
            experienceStmt.executeBatch();
            
            // Save new skills
            PreparedStatement skillsStmt = conn.prepareStatement(
                    "INSERT INTO resume_skills (resume_id, skill_item) VALUES (?, ?)");
            
            for (String skill : resume.getSkills()) {
                skillsStmt.setInt(1, resume.getId());
                skillsStmt.setString(2, skill);
                skillsStmt.addBatch();
            }
            skillsStmt.executeBatch();
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
