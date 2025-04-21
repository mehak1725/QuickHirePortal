#!/bin/bash

# Set application properties
export APP_PORT=5000
export APP_HOST=0.0.0.0

# Print basic info
echo "Starting QuickHire application..."
echo "================================="
echo "Host: $APP_HOST"
echo "Port: $APP_PORT"
echo ""

# Direct Maven execution with simpler configuration
mvn clean jetty:run -Djetty.http.port=$APP_PORT -Djetty.host=$APP_HOST