#!/bin/bash

# Set application properties
export APP_PORT=5000
export APP_HOST=0.0.0.0

# Print basic info
echo "Starting QuickHire application..."
echo "================================="
echo "Host: $APP_HOST"
echo "Port: $APP_PORT"
echo "Access URL: http://$APP_HOST:$APP_PORT/"
echo ""

# Create db directory if it doesn't exist
mkdir -p db

# Enable CORS for development
export CORS_ENABLED=true

# Configure Jetty for better external access
export JETTY_OPTS="-Dorg.eclipse.jetty.server.Request.maxFormKeys=1000 -Dorg.eclipse.jetty.server.Request.maxFormContentSize=10000000"

# Direct Maven execution with enhanced configuration
mvn clean jetty:run -Djetty.http.port=$APP_PORT -Djetty.host=$APP_HOST $JETTY_OPTS