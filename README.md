# Resume Upload and Search Web Application

This is a Java-based web application that enables recruiters to manage resumes more efficiently. Candidates can upload their resumes in `.docx` format, which are parsed automatically. Recruiters can search for candidates using keyword-based queries, and admins can verify candidate information.

## Features

- Candidate registration and login
- Resume upload (.docx) and automatic parsing using Apache POI
- Structured extraction of skills, experience, and education
- Keyword-based resume search for recruiters
- Admin verification of candidate profiles
- Role-based access for candidates, recruiters, and admins
- Real-time server health check endpoint
- Responsive and user-friendly interface

## Tech Stack

| Component   | Technology Used                     |
|-------------|--------------------------------------|
| Frontend    | HTML, CSS, JavaScript, JSP           |
| Backend     | Java Servlets (J2EE), Apache POI     |
| Database    | MySQL with JDBC                      |
| Server      | Jetty Embedded Server                |
| Utilities   | CORS Handling, Health Check Servlet  |

## Project Structure

### Java Packages (`src/main/java/com/example`)

- `CandidateLoginServlet.java`, `CandidateRegisterServlet.java`, `CandidateLogoutServlet.java`: Handle candidate authentication.
- `ResumeUploadServlet.java`, `ResumeParserUtil.java`: Manage file uploads and parse `.docx` resumes.
- `ResumeSearchServlet.java`, `ParsedResume.java`: Enable keyword search and represent resume data.
- `RecruiterLoginServlet.java`, `RecruiterLogoutServlet.java`: Handle recruiter authentication.
- `CandidateStatusServlet.java`, `CandidateVerifyServlet.java`: Support admin actions.
- `HealthCheckServlet.java`: Provides health status of the server.
- `DatabaseUtil.java`: Database connection utility.
- `CorsFilter.java`: Handles cross-origin requests.

### Webapp Directory (`src/main/webapp`)

- `login.html`, `register.html`: Candidate login/registration pages.
- `admin-login.html`, `admin.html`: Admin login and verification pages.
- `dashboard.html`: Candidate dashboard after login.
- `index.jsp`, `index.html`: Landing pages.
- `healthcheck.html`: Checks backend service status.
- `styles.css`, `script.js`: UI styling and behavior.


