#!/bin/bash
# Simple script to run all tests
# Created for easy test execution

echo "========================================"
echo "Running StudyMate Unit Tests"
echo "========================================"
echo

echo "[1/3] Cleaning previous build..."
./gradlew clean

echo
echo "[2/3] Running all unit tests..."
./gradlew test --stacktrace

echo
echo "[3/3] Opening test report..."
open app/build/reports/tests/testDebugUnitTest/index.html || xdg-open app/build/reports/tests/testDebugUnitTest/index.html

echo
echo "========================================"
echo "Tests completed!"
echo "Check the browser for detailed report."
echo "========================================"

