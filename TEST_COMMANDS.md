# Test Commands Cheat Sheet

Quick reference for running tests in the StudyMate project.

## Basic Commands

### Run All Tests
```bash
./gradlew test
```

### Run Tests with Detailed Output
```bash
./gradlew test --info
```

### Run Tests with Stack Traces
```bash
./gradlew test --stacktrace
```

### Clean and Test
```bash
./gradlew clean test
```

## Running Specific Tests

### Run Single Test Class
```bash
./gradlew test --tests com.example.studymate2.AuthenticationTest
```

### Run Single Test Method
```bash
./gradlew test --tests com.example.studymate2.AuthenticationTest.testLoginUser_ValidCredentials
```

### Run All Tests in Package
```bash
./gradlew test --tests com.example.studymate2.*
```

## Test Reports

### Generate HTML Report
```bash
./gradlew testDebugUnitTest
```

### Open Report (Windows)
```bash
start app\build\reports\tests\testDebugUnitTest\index.html
```

### Open Report (Linux/Mac)
```bash
open app/build/reports/tests/testDebugUnitTest/index.html
```

## Test Coverage

### Run Tests with Jacoco Coverage (if configured)
```bash
./gradlew testDebugUnitTestCoverage
```

## Debugging Tests

### Run Tests in Debug Mode
```bash
./gradlew test --debug-jvm
```

### Run Tests with Specific JVM Args
```bash
./gradlew test -Dtest.debug=true
```

## Continuous Testing

### Watch Mode (re-run on changes)
```bash
./gradlew test --continuous
```

## CI/CD Commands

### Simulate CI Build Locally
```bash
./gradlew clean build test --stacktrace
```

### Run Tests in Offline Mode
```bash
./gradlew test --offline
```

## Quick Test Filters

### Run Only Authentication Tests
```bash
./gradlew test --tests *AuthenticationTest
```

### Run Only API Tests
```bash
./gradlew test --tests *ApiServiceTest
```

### Run Only Validation Tests
```bash
./gradlew test --tests *ValidationTest
```

## Troubleshooting

### Clear Gradle Cache
```bash
./gradlew clean --no-build-cache
```

### Rebuild Everything
```bash
./gradlew clean build --rerun-tasks
```

### Check Dependencies
```bash
./gradlew dependencies
```

### Verify Test Setup
```bash
./gradlew tasks --all | grep test
```

## Performance

### Run Tests in Parallel
```bash
./gradlew test --parallel --max-workers=4
```

### Skip Other Tasks
```bash
./gradlew test -x lint
```

## Helper Scripts

### Windows
```bash
run_tests.bat
```

### Linux/Mac
```bash
./run_tests.sh
```

## Common Issues

### Issue: Tests not found
**Solution:** Ensure test files are in `app/src/test/java/`

### Issue: Out of memory
**Solution:** Add to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx2048m
```

### Issue: Firebase not initialized
**Solution:** Tests use mocks - Firebase doesn't need real initialization

### Issue: Slow tests
**Solution:** Use parallel execution:
```bash
./gradlew test --parallel
```

## Test Output Locations

- **HTML Report:** `app/build/reports/tests/testDebugUnitTest/index.html`
- **XML Results:** `app/build/test-results/testDebugUnitTest/`
- **Binary Results:** `app/build/test-results/testDebugUnitTest/binary/`

## Useful Combinations

### Quick Feedback Loop
```bash
./gradlew clean test --parallel --max-workers=4
```

### Full Test Run (like CI)
```bash
./gradlew clean build test --stacktrace --no-build-cache
```

### Test Specific Feature
```bash
./gradlew test --tests *Authentication* --info
```

## Tips

1. Use `--continuous` for TDD workflow
2. Use `--tests` to run specific tests during development
3. Always check the HTML report for detailed failure info
4. Use `--stacktrace` when debugging failures
5. Clear cache if tests behave unexpectedly

---

**Pro Tip:** Create an alias in your shell:
```bash
alias testall="./gradlew clean test --parallel"
alias testfast="./gradlew test --parallel"
```

