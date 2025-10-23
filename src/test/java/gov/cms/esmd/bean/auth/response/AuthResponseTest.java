package gov.cms.esmd.bean.auth.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AuthResponse class.
 * 
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
class AuthResponseTest {

    @Test
    void testDefaultConstructor_ShouldCreateEmptyResponse() {
        // When
        AuthResponse response = new AuthResponse();

        // Then
        assertThat(response.getAccess_token()).isNull();
        assertThat(response.getExpires_in()).isEqualTo(0);
        assertThat(response.getToken_type()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getStatusCode()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getResult()).isNull();
    }


    @Test
    void testSettersAndGetters_ShouldWorkCorrectly() {
        // Given
        AuthResponse response = new AuthResponse();
        String accessToken = "test-token";
        int expiresIn = 3600;
        String tokenType = "Bearer";
        String error = "test-error";
        String statusCode = "200";
        String message = "test-message";
        String result = "test-result";

        // When
        response.setAccess_token(accessToken);
        response.setExpires_in(expiresIn);
        response.setToken_type(tokenType);
        response.setError(error);
        response.setStatusCode(statusCode);
        response.setMessage(message);
        response.setResult(result);

        // Then
        assertThat(response.getAccess_token()).isEqualTo(accessToken);
        assertThat(response.getExpires_in()).isEqualTo(expiresIn);
        assertThat(response.getToken_type()).isEqualTo(tokenType);
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getStatusCode()).isEqualTo(statusCode);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getResult()).isEqualTo(result);
    }


    @Test
    void testEquals_WithSameValues_ShouldReturnTrue() {
        // Given
        AuthResponse response1 = new AuthResponse();
        response1.setAccess_token("token");
        response1.setExpires_in(3600);
        response1.setToken_type("Bearer");
        response1.setError("error");
        response1.setStatusCode("200");
        response1.setMessage("message");
        response1.setResult("result");

        AuthResponse response2 = new AuthResponse();
        response2.setAccess_token("token");
        response2.setExpires_in(3600);
        response2.setToken_type("Bearer");
        response2.setError("error");
        response2.setStatusCode("200");
        response2.setMessage("message");
        response2.setResult("result");

        // When & Then
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void testEquals_WithDifferentValues_ShouldReturnFalse() {
        // Given
        AuthResponse response1 = new AuthResponse();
        response1.setAccess_token("token1");

        AuthResponse response2 = new AuthResponse();
        response2.setAccess_token("token2");

        // When & Then
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    void testEquals_WithSameInstance_ShouldReturnTrue() {
        // Given
        AuthResponse response = new AuthResponse();
        response.setAccess_token("token");

        // When & Then
        assertThat(response).isEqualTo(response);
    }

    @Test
    void testEquals_WithNull_ShouldReturnFalse() {
        // Given
        AuthResponse response = new AuthResponse();

        // When & Then
        assertThat(response).isNotEqualTo(null);
    }

    @Test
    void testHashCode_WithSameValues_ShouldReturnSameHashCode() {
        // Given
        AuthResponse response1 = new AuthResponse();
        response1.setAccess_token("token");
        response1.setExpires_in(3600);

        AuthResponse response2 = new AuthResponse();
        response2.setAccess_token("token");
        response2.setExpires_in(3600);

        // When & Then
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void testToString_ShouldContainFieldNames() {
        // Given
        AuthResponse response = new AuthResponse();
        response.setAccess_token("test-token");
        response.setExpires_in(3600);
        response.setToken_type("Bearer");
        response.setError("test-error");

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("access_token");
        assertThat(toString).contains("test-token");
        assertThat(toString).contains("3600");
        assertThat(toString).contains("Bearer");
        assertThat(toString).contains("test-error");
    }
}
