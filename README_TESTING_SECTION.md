# Testing & CI/CD Section for Main README

Add this section to your main README.md file:

---

## 🧪 Testing

This project includes comprehensive unit tests covering all major functionality.

### Test Coverage

- **95 unit tests** across 6 test classes
- Authentication & user registration
- Settings persistence
- API integration with mocked responses
- Form validation & error handling
- Logging verification
- External SDK integration (Firebase, Google Sign-In, etc.)

### Running Tests

**Quick Start (Windows):**
```bash
run_tests.bat
```

**Quick Start (Linux/Mac):**
```bash
chmod +x run_tests.sh
./run_tests.sh
```

**Using Gradle:**
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests com.example.studymate2.AuthenticationTest

# Run with HTML report
./gradlew testDebugUnitTest
```

### View Test Reports

After running tests, open the HTML report:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

### Continuous Integration

This project uses GitHub Actions for automated testing. On every push or pull request:

- ✅ Code is compiled and built
- ✅ All 95 unit tests are executed
- ✅ Tests run across multiple Android SDK versions (28, 30, 33)
- ✅ Test results are automatically reported
- ✅ Build artifacts (APK & test reports) are uploaded
- ✅ Workflow fails if any test fails

**Check CI/CD status:** View the Actions tab in this repository

### Test Statistics

| Category | Tests | Description |
|----------|-------|-------------|
| Authentication | 11 | Login, registration, token handling |
| Settings | 12 | Preferences, notifications, language |
| API Integration | 12 | REST calls, error handling, timeouts |
| Validation | 22 | Email, password, form validation |
| Logging | 19 | Log verification, error tracking |
| SDK Integration | 19 | Firebase, Google Sign-In, Room DB |

### Documentation

For detailed testing information, see:
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Comprehensive testing documentation
- **[TEST_SUMMARY.md](TEST_SUMMARY.md)** - Quick implementation summary

---

## 📋 Project Status

![Build Status](https://github.com/YOUR_USERNAME/StudyMate_Part2/workflows/Android%20Build%20and%20Test/badge.svg)

- ✅ Unit tests passing
- ✅ CI/CD pipeline active
- ✅ Code quality checks enabled

---

