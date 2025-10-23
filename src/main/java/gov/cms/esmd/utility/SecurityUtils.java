package gov.cms.esmd.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Security utility class for input validation and sanitization.
 * 
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
public final class SecurityUtils {
    
    
    // Common validation patterns
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://[a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,})?(?:/.*)?$");
    
    // Size limits
    private static final int MAX_STRING_LENGTH = 1000;
    private static final int MAX_JSON_SIZE = 10_000_000; // 10MB
    private static final int MAX_FILENAME_LENGTH = 255;
    
    private SecurityUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates that a string contains only alphanumeric characters, underscores, and hyphens.
     * 
     * @param input the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the input is invalid
     */
    public static void validateAlphanumeric(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        
        if (input.length() > MAX_STRING_LENGTH) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum length of " + MAX_STRING_LENGTH);
        }
        
        if (!ALPHANUMERIC_PATTERN.matcher(input).matches()) {
            throw new IllegalArgumentException(fieldName + " contains invalid characters. Only alphanumeric characters, underscores, and hyphens are allowed");
        }
    }
    
    /**
     * Validates an email address format.
     * 
     * @param email the email to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the email is invalid
     */
    public static void validateEmail(String email, String fieldName) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        
        if (email.length() > MAX_STRING_LENGTH) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum length of " + MAX_STRING_LENGTH);
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(fieldName + " is not a valid email address");
        }
    }
    
    /**
     * Validates a URL format.
     * 
     * @param url the URL to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the URL is invalid
     */
    public static void validateUrl(String url, String fieldName) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        
        if (url.length() > MAX_STRING_LENGTH) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum length of " + MAX_STRING_LENGTH);
        }
        
        if (!URL_PATTERN.matcher(url).matches()) {
            throw new IllegalArgumentException(fieldName + " is not a valid URL");
        }
    }
    
    /**
     * Validates JSON payload size.
     * 
     * @param json the JSON string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the JSON is too large
     */
    public static void validateJsonSize(String json, String fieldName) {
        if (json == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        
        if (json.length() > MAX_JSON_SIZE) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum size of " + MAX_JSON_SIZE + " bytes");
        }
    }
    
    /**
     * Validates filename format and length.
     * 
     * @param filename the filename to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the filename is invalid
     */
    public static void validateFilename(String filename, String fieldName) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        
        if (filename.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException(fieldName + " exceeds maximum length of " + MAX_FILENAME_LENGTH);
        }
        
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException(fieldName + " contains invalid characters for security");
        }
    }
    
    /**
     * Sanitizes a string by removing potentially dangerous characters.
     * 
     * @param input the string to sanitize
     * @return the sanitized string
     */
    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove control characters and normalize whitespace
        return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    /**
     * Masks sensitive data for logging purposes.
     * 
     * @param value the value to mask
     * @return the masked value
     */
    public static String maskSensitiveData(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        if (value.length() <= 4) {
            return "****";
        }
        
        return "****" + value.substring(value.length() - 4);
    }
    
    /**
     * Validates that a string is not null or empty.
     * 
     * @param input the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the input is null or empty
     */
    public static void validateNotNullOrEmpty(String input, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Validates that a number is within a specified range.
     * 
     * @param value the number to validate
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is out of range
     */
    public static void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + ", got: " + value);
        }
    }
}
