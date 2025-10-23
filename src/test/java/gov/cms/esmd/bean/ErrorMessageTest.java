package gov.cms.esmd.bean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ErrorMessage class.
 * 
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
class ErrorMessageTest {


    @Test
    void testConstructorWithParameters_ShouldSetValues() {
        // Given
        String errorCode = "ERR001";
        String errorName = "Validation Error";
        String errorDescription = "Required field is missing";

        // When
        ErrorMessage errorMessage = new ErrorMessage(errorCode, errorName, errorDescription);

        // Then
        assertThat(errorMessage.getErrorCode()).isEqualTo(errorCode);
        assertThat(errorMessage.getErrorName()).isEqualTo(errorName);
        assertThat(errorMessage.getErrorDescription()).isEqualTo(errorDescription);
    }

    @Test
    void testSettersAndGetters_ShouldWorkCorrectly() {
        // Given
        ErrorMessage errorMessage = new ErrorMessage("ERR001", "Test Error", "Test Description");
        String errorCode = "ERR002";
        String errorName = "System Error";
        String errorDescription = "Internal server error occurred";

        // When
        errorMessage.setErrorCode(errorCode);
        errorMessage.setErrorName(errorName);
        errorMessage.setErrorDescription(errorDescription);

        // Then
        assertThat(errorMessage.getErrorCode()).isEqualTo(errorCode);
        assertThat(errorMessage.getErrorName()).isEqualTo(errorName);
        assertThat(errorMessage.getErrorDescription()).isEqualTo(errorDescription);
    }


    @Test
    void testEquals_WithSameValues_ShouldReturnTrue() {
        // Given
        ErrorMessage errorMessage1 = new ErrorMessage("ERR001", "Test Error", "Test Description");
        ErrorMessage errorMessage2 = new ErrorMessage("ERR001", "Test Error", "Test Description");

        // When & Then
        assertThat(errorMessage1).isEqualTo(errorMessage2);
    }

    @Test
    void testEquals_WithDifferentValues_ShouldReturnFalse() {
        // Given
        ErrorMessage errorMessage1 = new ErrorMessage("ERR001", "Test Error", "Test Description");
        ErrorMessage errorMessage2 = new ErrorMessage("ERR002", "Test Error", "Test Description");

        // When & Then
        assertThat(errorMessage1).isNotEqualTo(errorMessage2);
    }

    @Test
    void testEquals_WithSameInstance_ShouldReturnTrue() {
        // Given
        ErrorMessage errorMessage = new ErrorMessage("ERR001", "Test Error", "Test Description");

        // When & Then
        assertThat(errorMessage).isEqualTo(errorMessage);
    }

    @Test
    void testEquals_WithNull_ShouldReturnFalse() {
        // Given
        ErrorMessage errorMessage = new ErrorMessage("ERR001", "Test Error", "Test Description");

        // When & Then
        assertThat(errorMessage).isNotEqualTo(null);
    }

    @Test
    void testEquals_WithDifferentClass_ShouldReturnFalse() {
        // Given
        ErrorMessage errorMessage = new ErrorMessage("ERR001", "Test Error", "Test Description");
        String otherObject = "not-error-message";

        // When & Then
        assertThat(errorMessage).isNotEqualTo(otherObject);
    }

    @Test
    void testHashCode_WithSameValues_ShouldReturnSameHashCode() {
        // Given
        ErrorMessage errorMessage1 = new ErrorMessage("ERR001", "Test Error", "Test Description");
        ErrorMessage errorMessage2 = new ErrorMessage("ERR001", "Test Error", "Test Description");

        // When & Then
        assertThat(errorMessage1.hashCode()).isEqualTo(errorMessage2.hashCode());
    }

    @Test
    void testHashCode_WithDifferentValues_ShouldReturnDifferentHashCode() {
        // Given
        ErrorMessage errorMessage1 = new ErrorMessage("ERR001", "Test Error", "Test Description");
        ErrorMessage errorMessage2 = new ErrorMessage("ERR002", "Test Error", "Test Description");

        // When & Then
        assertThat(errorMessage1.hashCode()).isNotEqualTo(errorMessage2.hashCode());
    }

    @Test
    void testToString_ShouldContainAllFields() {
        // Given
        ErrorMessage errorMessage = new ErrorMessage("ERR001", "Test Error", "Test Description");

        // When
        String toString = errorMessage.toString();

        // Then
        assertThat(toString).contains("ERR001");
        assertThat(toString).contains("Test Error");
        assertThat(toString).contains("Test Description");
        assertThat(toString).contains("ErrorMessage");
    }

}
