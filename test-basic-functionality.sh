#!/bin/bash

# Basic functionality test script for DistributedStorage
# Usage: ./test-basic-functionality.sh

set -e

echo "🧪 Testing DistributedStorage Basic Functionality"
echo "================================================"

# Configuration
LOAD_BALANCER_URL="http://localhost:8080"
USER_ID="1"
TEST_FILE="test-upload.txt"

# Create test file
echo "Creating test file..."
echo "This is a test file for DistributedStorage system - $(date)" > $TEST_FILE

# Test 1: Health Check
echo "1️⃣  Testing Load Balancer Health..."
if curl -s -f "$LOAD_BALANCER_URL/actuator/health" > /dev/null; then
    echo "✅ Load balancer is healthy"
else
    echo "❌ Load balancer health check failed"
    exit 1
fi

# Test 2: Storage Nodes Health
echo "2️⃣  Testing Storage Nodes Health..."
for port in 8081 8082 8083; do
    if curl -s -f "http://localhost:$port/api/v1/health/status" > /dev/null; then
        echo "✅ Storage node on port $port is healthy"
    else
        echo "⚠️  Storage node on port $port is not responding"
    fi
done

# Test 3: File Upload
echo "3️⃣  Testing File Upload..."
UPLOAD_RESPONSE=$(curl -s -X POST \
    -H "X-User-ID: $USER_ID" \
    -F "file=@$TEST_FILE" \
    "$LOAD_BALANCER_URL/api/v1/files/upload")

if [[ $UPLOAD_RESPONSE == *"fileId"* ]]; then
    FILE_ID=$(echo $UPLOAD_RESPONSE | grep -o '"fileId":[0-9]*' | grep -o '[0-9]*')
    echo "✅ File uploaded successfully with ID: $FILE_ID"
else
    echo "❌ File upload failed"
    echo "Response: $UPLOAD_RESPONSE"
    exit 1
fi

# Test 4: File Download
echo "4️⃣  Testing File Download..."
if curl -s -X GET \
    -H "X-User-ID: $USER_ID" \
    "$LOAD_BALANCER_URL/api/v1/files/$FILE_ID" \
    --output "downloaded-$TEST_FILE"; then
    echo "✅ File downloaded successfully"

    # Verify content
    if diff "$TEST_FILE" "downloaded-$TEST_FILE" > /dev/null; then
        echo "✅ Downloaded file content matches original"
    else
        echo "❌ Downloaded file content differs from original"
    fi
else
    echo "❌ File download failed"
fi

# Test 5: System Metrics
echo "5️⃣  Testing System Metrics..."
if curl -s -f "$LOAD_BALANCER_URL/api/v1/metrics/stats" > /dev/null; then
    echo "✅ System metrics accessible"
else
    echo "❌ System metrics not accessible"
fi

# Test 6: File Deletion
echo "6️⃣  Testing File Deletion..."
if curl -s -X DELETE \
    -H "X-User-ID: $USER_ID" \
    "$LOAD_BALANCER_URL/api/v1/files/$FILE_ID" > /dev/null; then
    echo "✅ File deleted successfully"
else
    echo "❌ File deletion failed"
fi

# Cleanup
echo "🧹 Cleaning up test files..."
rm -f "$TEST_FILE" "downloaded-$TEST_FILE"

echo ""
echo "🎉 Basic functionality test completed!"
echo "ℹ️  For more comprehensive testing, see DEVELOPER_SETUP.md"