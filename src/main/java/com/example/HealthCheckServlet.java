package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

import org.json.JSONObject;

/**
 * Servlet for health check and status monitoring
 */
@WebServlet("/api/healthcheck")
public class HealthCheckServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type
        response.setContentType("application/json");
        
        JSONObject healthStatus = new JSONObject();
        healthStatus.put("status", "UP");
        healthStatus.put("serverTime", System.currentTimeMillis());
        healthStatus.put("environment", "Replit");
        
        // Check database connectivity
        boolean dbStatus = checkDatabaseConnection();
        healthStatus.put("database", dbStatus ? "connected" : "disconnected");
        
        // System info
        JSONObject system = new JSONObject();
        system.put("java.version", System.getProperty("java.version"));
        system.put("os.name", System.getProperty("os.name"));
        system.put("available_processors", Runtime.getRuntime().availableProcessors());
        system.put("free_memory", Runtime.getRuntime().freeMemory());
        system.put("total_memory", Runtime.getRuntime().totalMemory());
        
        healthStatus.put("system", system);
        
        // Add server connectivity info
        healthStatus.put("serverAddress", request.getLocalAddr());
        healthStatus.put("serverPort", request.getLocalPort());
        healthStatus.put("clientAddress", request.getRemoteAddr());
        
        response.getWriter().write(healthStatus.toString(2)); // Pretty print with 2 space indentation
    }
    
    /**
     * Check database connection
     * @return true if connected, false otherwise
     */
    private boolean checkDatabaseConnection() {
        try {
            // Use the same JDBC URL as in DatabaseUtil
            String jdbcUrl = "jdbc:sqlite:db/quickhire.db";
            Connection conn = DriverManager.getConnection(jdbcUrl);
            boolean isConnected = conn != null && !conn.isClosed();
            if (conn != null) {
                conn.close();
            }
            return isConnected;
        } catch (SQLException e) {
            System.err.println("Database health check failed: " + e.getMessage());
            return false;
        }
    }
}