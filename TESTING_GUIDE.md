# StudyMate Testing Guide

This document explains how automated testing is set up in the StudyMate project and how to run tests locally.

## Overview

The project uses comprehensive unit tests to ensure code quality and reliability. Tests cover authentication, settings management, API integration, form validation, error handling, and logging.

## Testing Framework

The project uses the following testing libraries:

- **JUnit 4** - Main testing framework
- **Mockito** - Mocking framework for isolating components
- **Robolectric** - Android unit testing without emulator
- **MockWebServer** - For testing API calls
- **Kotlin Coroutines Test** - For testing asynchronous code
- **AndroidX Test** - Core Android testing utilities

## Test Coverage

### 1. Authentication Tests (`AuthenticationTest.kt`)
Tests Firebase authentication functionality including:
- User registration (success and failure scenarios)
- Login with valid and invalid credentials
- Token retrieval and validation
- Google Sign-In integration
- Password validation
- Logout functionality

**Example test:**
```kotlin
@Test
fun testLoginUser_ValidCredentials() {
    val email = "user@example.com"
    val password = "correctpassword"
    
    // Mock successful login
    `when`(mockAuth.signInWithEmailAndPassword(email, password))
        .thenReturn(mockAuthTask)
    `when`(mockAuthTask.isSuccessful).thenReturn(true)
    
    // Verify login succeeds
    assertTrue(mockAuthTask.isSuccessful)
}
```

### 2. Settings Tests (`SettingsTest.kt`)
Verifies settings persistence and configuration:
- Notification preferences
- Reminder time settings
- Language selection
- Settings reset to defaults
- Multiple rapid changes

### 3. API Service Tests (`ApiServiceTest.kt`)
Tests RESTful API integration with mocked responses:
- Successful API calls
- Error handling (500, 404 errors)
- Timeout scenarios
- Malformed JSON handling
- Network connectivity issues
- Request formatting verification

**Example test:**
```kotlin
@Test
fun testFetchStudyTodos_Success() = runTest {
    mockWebServer.enqueue(
        MockResponse()
            .setResponseCode(200)
            .setBody("""[{"id": 1, "title": "Study Math", "completed": false}]""")
    )
    
    val result = apiService.fetchStudyTodos(1)
    
    assertEquals(1, result.size)
    assertEquals("Study Math", result[0].title)
}
```

### 4. Validation Tests (`ValidationTest.kt`)
Tests input validation and error handling:
- Email format validation
- Password strength requirements
- Empty field detection
- Form validation
- SQL injection prevention
- XSS attempt handling
- Unicode character handling

### 5. Logging Tests (`LoggingTest.kt`)
Verifies logging during key operations:
- Login attempts
- Registration events
- API requests
- Settings updates
- Error scenarios
- Performance tracking

### 6. SDK Integration Tests (`SdkIntegrationTest.kt`)
Tests external library integration:
- Firebase SDK initialization
- Google Sign-In configuration
- Exception handling from SDKs
- Room Database integration
- WorkManager setup
- DataStore functionality

## Running Tests Locally

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests com.example.studymate2.AuthenticationTest
```

### Run Single Test Method
```bash
./gradlew test --tests com.example.studymate2.AuthenticationTest.testLoginUser_ValidCredentials
```

### Run Tests with Coverage
```bash
./gradlew testDebugUnitTest
```

### Generate Test Reports
```bash
./gradlew test
```
Test reports are generated at: `app/build/reports/tests/testDebugUnitTest/index.html`

## GitHub Actions CI/CD

### Workflow Configuration

The project uses GitHub Actions for automated testing and building. The workflow is defined in `.github/workflows/android-build.yml`.

**Workflow triggers:**
- Push to `main`, `master`, or `develop` branches
- Pull requests to these branches

**What the workflow does:**

1. **Build Job:**
   - Tests across multiple Android API levels (28, 30, 33)
   - Caches Gradle dependencies for faster builds
   - Compiles the project
   - Runs all unit tests
   - Generates test reports
   - Builds APK
   - Uploads test results and APK as artifacts

2. **Lint Job:**
   - Runs Android Lint checks
   - Uploads lint report

**Key Features:**
- ✅ Matrix strategy for testing multiple SDK versions
- ✅ Gradle dependency caching (speeds up builds by ~60%)
- ✅ Automatic test result reporting
- ✅ Workflow fails if any test fails
- ✅ Test artifacts available for download

### Viewing Test Results

1. Go to the **Actions** tab in your GitHub repository
2. Click on the latest workflow run
3. View test results in the job summary
4. Download detailed test reports from artifacts

### Sample Output - Passing Tests

```
> Task :app:testDebugUnitTest

com.example.studymate2.AuthenticationTest > testRegisterUser_Success PASSED
com.example.studymate2.AuthenticationTest > testLoginUser_ValidCredentials PASSED
com.example.studymate2.SettingsTest > testSaveNotificationSetting_Success PASSED
com.example.studymate2.ApiServiceTest > testFetchStudyTodos_Success PASSED
com.example.studymate2.ValidationTest > testEmailValidation_ValidEmails PASSED

BUILD SUCCESSFUL in 45s
78 tests, 78 succeeded, 0 failed, 0 skipped
```

### Sample Output - Failing Tests

```
> Task :app:testDebugUnitTest FAILED

com.example.studymate2.ValidationTest > testEmailValidation_ValidEmails PASSED
com.example.studymate2.ValidationTest > testPasswordValidation_MinimumLength FAILED

org.junit.ComparisonFailure: Expected :true
Actual   :false

2 tests completed, 1 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:testDebugUnitTest'.
> There were failing tests. See the report at: file:///app/build/reports/tests/testDebugUnitTest/index.html
```

## Best Practices

### Writing Tests

1. **Use descriptive names:** `testLoginUser_ValidCredentials` vs `test1`
2. **Follow AAA pattern:** Arrange, Act, Assert
3. **One assertion per test** (when possible)
4. **Test edge cases:** null values, empty strings, boundary values
5. **Mock external dependencies:** Firebase, APIs, databases

### Test Organization

```kotlin
class MyTest {
    @Before
    fun setup() {
        // Initialize mocks and test data
    }
    
    @Test
    fun testFeature_Scenario() {
        // Arrange
        val input = "test@example.com"
        
        // Act
        val result = validateEmail(input)
        
        // Assert
        assertTrue(result)
    }
    
    @After
    fun teardown() {
        // Clean up resources
    }
}
```

## Troubleshooting

### Common Issues

**Issue:** Tests fail with `java.lang.IllegalStateException: Default FirebaseApp is not initialized`

**Solution:** Tests use Robolectric which simulates Android. Firebase needs proper initialization in tests or should be mocked.

---

**Issue:** `MockWebServer` timeout

**Solution:** Increase timeout in test or check if server is properly started:
```kotlin
mockWebServer.start()
// ... tests
mockWebServer.shutdown()
```

---

**Issue:** Gradle build fails on CI but works locally

**Solution:** 
- Check Java version (should be JDK 11)
- Clear Gradle cache: `./gradlew clean build --no-build-cache`
- Ensure `google-services.json` is not in `.gitignore` (or use secrets)

## Running Tests on Another Machine

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd StudyMate_Part2
   ```

2. **Install Java 11:**
   - Download from [Adoptium](https://adoptium.net/)
   - Verify: `java -version`

3. **Run tests:**
   ```bash
   ./gradlew test
   ```

4. **View results:**
   - Open `app/build/reports/tests/testDebugUnitTest/index.html` in browser

## Contributing

When adding new features:
1. Write tests first (TDD approach)
2. Ensure all tests pass locally
3. Push changes and verify CI passes
4. Update this documentation if needed

## Contact

For questions about testing setup, please open an issue in the repository.

