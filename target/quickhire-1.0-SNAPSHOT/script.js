/**
 * QuickHire - Resume Parser & Job Portal
 * Global JavaScript functions and utilities
 */

/**
 * Capitalize the first letter of a string
 * @param {string} string - The input string
 * @return {string} The string with first letter capitalized
 */
function capitalizeFirstLetter(string) {
    if (!string) return '';
    return string.charAt(0).toUpperCase() + string.slice(1);
}

/**
 * Format a phone number to a standard format
 * @param {string} phone - The phone number to format
 * @return {string} The formatted phone number
 */
function formatPhoneNumber(phone) {
    if (!phone) return '';
    
    // Remove all non-digit characters
    const cleaned = phone.replace(/\D/g, '');
    
    // Format based on length
    if (cleaned.length === 10) {
        return `(${cleaned.substring(0, 3)}) ${cleaned.substring(3, 6)}-${cleaned.substring(6, 10)}`;
    } else if (cleaned.length === 11 && cleaned.charAt(0) === '1') {
        return `+1 (${cleaned.substring(1, 4)}) ${cleaned.substring(4, 7)}-${cleaned.substring(7, 11)}`;
    }
    
    // Return original if not matching standard formats
    return phone;
}

/**
 * Show an alert message
 * @param {string} message - The message to display
 * @param {string} type - The type of alert ('success' or 'error')
 * @param {string} elementId - The ID of the alert container
 */
function showAlert(message, type, elementId) {
    const alertBox = document.getElementById(elementId);
    if (!alertBox) return;
    
    alertBox.className = 'alert-box ' + type;
    alertBox.textContent = message;
    alertBox.style.display = 'block';
    
    // Hide alert after 5 seconds for success messages
    if (type === 'success') {
        setTimeout(function() {
            alertBox.style.display = 'none';
        }, 5000);
    }
}

/**
 * Validate an email address
 * @param {string} email - The email to validate
 * @return {boolean} True if valid, false otherwise
 */
function isValidEmail(email) {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailPattern.test(email);
}

/**
 * Validate password strength
 * @param {string} password - The password to validate
 * @return {boolean} True if valid, false otherwise
 */
function isValidPassword(password) {
    return password && password.length >= 6;
}

/**
 * Create a skill tag element
 * @param {string} skill - The skill text
 * @return {HTMLElement} The created skill tag element
 */
function createSkillTag(skill) {
    const span = document.createElement('span');
    span.className = 'skill-tag';
    span.textContent = skill;
    return span;
}

/**
 * Highlight matching text in a string
 * @param {string} text - The original text
 * @param {string} keyword - The keyword to highlight
 * @return {string} HTML string with highlighted keyword
 */
function highlightKeyword(text, keyword) {
    if (!text || !keyword) return text;
    
    const regex = new RegExp(`(${keyword})`, 'gi');
    return text.replace(regex, '<mark>$1</mark>');
}

/**
 * Format a date string to a more readable format
 * @param {string} dateString - The date string to format
 * @return {string} The formatted date string
 */
function formatDate(dateString) {
    if (!dateString) return '';
    
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

/**
 * Create a status badge element
 * @param {string} status - The status text ('pending', 'shortlisted', 'rejected')
 * @return {HTMLElement} The created status badge element
 */
function createStatusBadge(status) {
    const span = document.createElement('span');
    span.className = `status-badge ${status}`;
    span.textContent = capitalizeFirstLetter(status);
    return span;
}

/**
 * Check if a user is authenticated
 * @param {string} userType - The type of user to check ('candidate' or 'recruiter')
 * @param {Function} redirectCallback - Callback to handle redirect if not authenticated
 */
function checkAuthentication(userType, redirectCallback) {
    const endpoint = userType === 'recruiter' ? '/api/resume/search' : '/api/resume/view';
    
    fetch(endpoint)
        .then(response => {
            if (!response.ok) {
                // Not authenticated, redirect
                redirectCallback();
            }
        })
        .catch(error => {
            console.error('Authentication check error:', error);
            redirectCallback();
        });
}

/**
 * Load and display resume data
 * @param {string} resumeId - The ID of the resume to load (optional)
 * @param {Function} successCallback - Callback for successful data load
 * @param {Function} errorCallback - Callback for error handling
 */
function loadResumeData(resumeId, successCallback, errorCallback) {
    const url = resumeId ? `/api/resume/view?id=${resumeId}` : '/api/resume/view';
    
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch resume data');
            }
            return response.json();
        })
        .then(data => {
            if (data.success && data.resume) {
                successCallback(data.resume);
            } else {
                errorCallback('No resume found');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            errorCallback(error.message || 'Error loading resume data');
        });
}

/**
 * Search resumes by keyword
 * @param {string} keyword - The keyword to search for
 * @param {Function} successCallback - Callback for successful search
 * @param {Function} errorCallback - Callback for error handling
 */
function searchResumes(keyword, successCallback, errorCallback) {
    fetch(`/api/resume/search?keyword=${encodeURIComponent(keyword)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch resumes');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                successCallback(data.resumes, data.count, data.keyword);
            } else {
                errorCallback('Search failed');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            errorCallback(error.message || 'Error searching resumes');
        });
}

/**
 * Update a resume's status
 * @param {string} resumeId - The ID of the resume to update
 * @param {string} status - The new status ('shortlisted', 'rejected', 'pending')
 * @param {Function} successCallback - Callback for successful update
 * @param {Function} errorCallback - Callback for error handling
 */
function updateResumeStatus(resumeId, status, successCallback, errorCallback) {
    const formData = new FormData();
    formData.append('resumeId', resumeId);
    formData.append('status', status);
    
    fetch('/api/resume/status', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            successCallback(data);
        } else {
            errorCallback(data.message || 'Status update failed');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        errorCallback(error.message || 'Error updating status');
    });
}

/**
 * Log out the current user
 * @param {string} userType - The type of user to log out ('candidate' or 'recruiter')
 * @param {Function} redirectCallback - Callback to handle redirect after logout
 */
function logoutUser(userType, redirectCallback) {
    const endpoint = userType === 'recruiter' ? '/api/recruiter/logout' : '/api/candidate/logout';
    
    fetch(endpoint, {
        method: 'POST'
    })
    .then(() => {
        // Clear any stored data and redirect
        if (userType === 'recruiter') {
            localStorage.removeItem('recruiterId');
        } else {
            localStorage.removeItem('candidateName');
            localStorage.removeItem('candidateId');
            localStorage.removeItem('hasResume');
        }
        redirectCallback();
    })
    .catch(error => {
        console.error('Error:', error);
        // Still redirect even if logout fails
        redirectCallback();
    });
}

/**
 * Create a labeled info item element
 * @param {string} label - The label text
 * @param {string} value - The value text
 * @return {HTMLElement} The created info item element
 */
function createInfoItem(label, value) {
    const div = document.createElement('div');
    div.className = 'info-item';
    
    const labelSpan = document.createElement('span');
    labelSpan.className = 'label';
    labelSpan.textContent = label;
    
    const valueSpan = document.createElement('span');
    valueSpan.className = 'value';
    valueSpan.textContent = value || '-';
    
    div.appendChild(labelSpan);
    div.appendChild(valueSpan);
    
    return div;
}

/**
 * Display a loading spinner
 * @param {HTMLElement} container - The container to show the spinner in
 * @param {string} message - The loading message to display
 */
function showLoading(container, message) {
    container.innerHTML = '';
    
    const loadingDiv = document.createElement('div');
    loadingDiv.className = 'resume-loading';
    
    const icon = document.createElement('i');
    icon.setAttribute('data-feather', 'loader');
    
    const text = document.createElement('p');
    text.textContent = message || 'Loading...';
    
    loadingDiv.appendChild(icon);
    loadingDiv.appendChild(text);
    container.appendChild(loadingDiv);
    
    // Initialize Feather icons
    if (typeof feather !== 'undefined') {
        feather.replace();
    }
}

/**
 * Show an empty state message
 * @param {HTMLElement} container - The container to show the empty state in
 * @param {string} icon - The Feather icon name
 * @param {string} title - The title text
 * @param {string} message - The description text
 */
function showEmptyState(container, icon, title, message) {
    container.innerHTML = '';
    
    const emptyDiv = document.createElement('div');
    emptyDiv.className = 'resumes-empty';
    emptyDiv.style.display = 'flex';
    
    const iconElem = document.createElement('i');
    iconElem.setAttribute('data-feather', icon || 'file-text');
    
    const titleElem = document.createElement('h3');
    titleElem.textContent = title || 'No Data Found';
    
    const messageElem = document.createElement('p');
    messageElem.textContent = message || 'No data is available.';
    
    emptyDiv.appendChild(iconElem);
    emptyDiv.appendChild(titleElem);
    emptyDiv.appendChild(messageElem);
    container.appendChild(emptyDiv);
    
    // Initialize Feather icons
    if (typeof feather !== 'undefined') {
        feather.replace();
    }
}

// Execute when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Feather icons if they exist
    if (typeof feather !== 'undefined') {
        feather.replace();
    }
});
