# ESMD Wiser Utility

A Java utility library for interacting with the Electronic Submission of Medical Documentation (esMD) Wiser API. 
This utility provides comprehensive functionality for uploading, downloading, and managing Wiser requests and responses through the esMD system.

## 🚀 Features

- **File Upload**: Upload Wiser requests to esMD with presigned URL support
- **File Download**: Download Wiser responses from esMD with automatic extraction
- **Authentication**: OAuth-based authentication with multiple scopes
- **Notifications**: Send pickup and status notifications to esMD
- **Security**: Comprehensive input validation and security utilities
- **Configuration**: YAML-based configuration management
- **Logging**: Structured logging with sensitive data masking

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Reference](#api-reference)
- [Security](#security)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## 🔧 Prerequisites

- **Java 11** or higher
- **Maven 3.6+**
- **Access to esMD API** (dev, val, uat, or prod environments)
- **Valid credentials** for esMD authentication

## 📦 Installation

### Clone the Repository

```bash
git clone <repository-url>
cd esmd-wiser-utility
```

### Build the Project

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Package the Application

```bash
mvn clean package
```

## ⚙️ Configuration

The application uses a YAML configuration file (`src/main/resources/api-properties.yml`) for all settings:

### Environment Configuration

```yaml
api:
  environment:
    dev: https://dev.cpiapigateway.cms.gov/api/esmd/v1
    val: https://val.cpiapigateway.cms.gov/api/esmd/v1
    uat: https://val.cpiapigateway.cms.gov/api/esmd/ext/v1
    prod: https://cpiapigateway.cms.gov/api/esmd/ext/v1
```

### User Information

```yaml
userinfo:
  mailboxid: WSDEV101
  clientid: your-client-id
  clientsecret: your-client-secret
```

### File Paths

```yaml
api:
  file-download:
    local-path: c:\\esmd\\wiser\\downloads
  file-upload:
    local-path: c:\\esmd\\wiser\\upload
    partsize: 200857600
```

## 🚀 Usage

### Basic Upload Example

```java
import gov.cms.esmd.rc.impl.UploadImpl;

public class Example {
    public static void main(String[] args) throws Exception {
        UploadImpl uploadImpl = new UploadImpl();
        StatusDetail result = uploadImpl.uploadWiserRequestsToesMD("dev");
        System.out.println("Upload Status: " + result.getStatus());
    }
}
```

### Basic Download Example

```java
import gov.cms.esmd.rc.impl.DownloadImpl;
import java.util.List;

public class Example {
    public static void main(String[] args) throws Exception {
        DownloadImpl downloadImpl = new DownloadImpl();
        List<StatusDetail> results = downloadImpl.downloadWiserRequestsFromesMD("dev");
        
        for (StatusDetail result : results) {
            System.out.println("Download Status: " + result.getStatus());
            System.out.println("File: " + result.getEsMDTransactionID());
        }
    }
}
```

### Notification Example

```java
import gov.cms.esmd.rc.impl.NotificationsImpl;

public class Example {
    public static void main(String[] args) throws Exception {
        NotificationsImpl notifications = new NotificationsImpl();
        
        // Create pickup notification
        String notificationJson = notifications.createPickupNotification(
            "PICKUP_CONFIRMATION_WISER_MAC",
            "WSDEV101",
            "ZKW0007260517EC",
            "ES9999.D.L1.EZKW0007260517EC.ESMD2.D071425.T2219020.zip"
        );
        
        // Send notification
        NotificationResponse response = notifications.sendNotificationToESMD(
            "dev", notificationJson, "PICKUP"
        );
    }
}
```

## 📚 API Reference

### Core Classes

#### UploadImpl
Handles uploading Wiser requests to esMD.

**Methods:**
- `uploadWiserRequestsToesMD(String environment)` - Upload files from local directory

#### DownloadImpl
Handles downloading Wiser responses from esMD.

**Methods:**
- `downloadWiserRequestsFromesMD(String environment)` - Download and extract files

#### NotificationsImpl
Handles sending notifications to esMD.

**Methods:**
- `sendNotificationToESMD(String environment, String jsonMessage, String notificationType)` - Send notification
- `createPickupNotification(String notificationType, String senderRoutingID, String esMDTransactionId, String filename)` - Create pickup notification JSON

### Utility Classes

#### ConfigurationManager
Manages YAML configuration loading and validation.

#### SecurityUtils
Provides input validation and sanitization utilities.

#### FileUtils
Handles file operations and directory management.

#### JSONUtility
Provides JSON serialization/deserialization using Gson.

## 🔒 Security

### Security Features

- **Input Validation**: Comprehensive validation using regex patterns
- **Data Sanitization**: Automatic sanitization of user inputs
- **Sensitive Data Masking**: Automatic masking of sensitive information in logs
- **Path Traversal Prevention**: Protection against directory traversal attacks
- **Size Limits**: Enforced limits on file sizes and JSON payloads

### Security Best Practices

1. **Update Dependencies**: Regularly update all dependencies, especially SnakeYAML
2. **Secure Configuration**: Store sensitive credentials in secure locations
3. **Network Security**: Use HTTPS for all API communications
4. **Input Validation**: Always validate user inputs before processing
5. **Error Handling**: Never expose sensitive information in error messages

### Critical Security Notes

⚠️ **IMPORTANT**: The current version uses SnakeYAML 2.2, which has a critical security vulnerability (CVE-2022-1471). **Update to SnakeYAML 2.4+ immediately**.

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ErrorMessageTest

# Run with coverage
mvn test jacoco:report
```

### Test Structure

- **Unit Tests**: Located in `src/test/java`
- **Test Coverage**: Comprehensive coverage of core functionality
- **Mocking**: Uses Mockito for API client testing
- **Assertions**: Uses AssertJ for fluent assertions

## 📁 Project Structure

```
esmd-wiser-utility/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── gov/cms/esmd/
│   │   │       ├── auth/                    # Authentication classes
│   │   │       ├── bean/                    # Data transfer objects
│   │   │       │   ├── adminerror/         # Admin error beans
│   │   │       │   ├── auth/               # Authentication beans
│   │   │       │   ├── parejectjson/       # PA reject JSON beans
│   │   │       │   ├── rcmetadata/         # RC metadata beans
│   │   │       │   └── response/           # Response beans
│   │   │       ├── notifications/         # Notification implementations
│   │   │       ├── rc/                     # RC (Request/Response) classes
│   │   │       │   ├── api/client/        # API client implementations
│   │   │       │   └── impl/              # Implementation classes
│   │   │       └── utility/               # Utility classes
│   │   └── resources/
│   │       ├── api-properties.yml        # Configuration file
│   │       └── logback.xml               # Logging configuration
│   └── test/
│       └── java/                        # Test classes
├── pom.xml                             # Maven configuration
├── SECURITY_IMPROVEMENTS.md           # Security documentation
└── README.md                          # This file
```

## 🔧 Dependencies

### Core Dependencies

- **SnakeYAML 2.2** - YAML configuration parsing
- **Gson 2.10.1** - JSON processing
- **Apache HttpClient 4.5.13** - HTTP client
- **Commons Lang3 3.14.0** - String utilities
- **Commons IO 2.16.1** - File utilities
- **Logback 1.2.6** - Logging framework

### Test Dependencies

- **JUnit 5.10.0** - Testing framework
- **Mockito 5.5.0** - Mocking framework
- **AssertJ 3.24.2** - Fluent assertions

## 🚨 Known Issues

1. **SnakeYAML Security Vulnerability**: Update to version 2.4+ immediately
2. **HTTP Client Version**: Consider updating Apache HttpClient to latest version
3. **Logback Version**: Update to latest version for security patches

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the terms specified in the project documentation.

## 📞 Support

For support and questions:
- Check the test cases for usage examples
- Ensure all dependencies are up to date

## 🔄 Version History

- **v1.0-SNAPSHOT**: Initial release with core functionality
  - Upload/Download capabilities
  - Authentication system
  - Notification support
  - Security utilities
  - Comprehensive testing

---

**Note**: This utility is designed for use with the CMS esMD system. Ensure you have proper authorization and credentials before using in production environments.
