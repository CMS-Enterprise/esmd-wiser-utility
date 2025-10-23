package gov.cms.esmd.bean.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AuthInfoBean class.
 * 
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
class AuthInfoBeanTest {

    @Test
    void testDefaultConstructor_ShouldCreateEmptyBean() {
        // When
        AuthInfoBean authInfo = new AuthInfoBean();

        // Then
        assertThat(authInfo.getClientkey()).isNull();
        assertThat(authInfo.getClientsecret()).isNull();
    }

    @Test
    void testSettersAndGetters_ShouldWorkCorrectly() {
        // Given
        AuthInfoBean authInfo = new AuthInfoBean();
        String clientkey = "test-key";
        String clientsecret = "test-secret";

        // When
        authInfo.setClientkey(clientkey);
        authInfo.setClientsecret(clientsecret);

        // Then
        assertThat(authInfo.getClientkey()).isEqualTo(clientkey);
        assertThat(authInfo.getClientsecret()).isEqualTo(clientsecret);
    }

    @Test
    void testEquals_WithSameValues_ShouldReturnTrue() {
        // Given
        AuthInfoBean authInfo1 = new AuthInfoBean();
        authInfo1.setClientkey("key1");
        authInfo1.setClientsecret("secret1");
        
        AuthInfoBean authInfo2 = new AuthInfoBean();
        authInfo2.setClientkey("key1");
        authInfo2.setClientsecret("secret1");

        // When & Then
        assertThat(authInfo1).isEqualTo(authInfo2);
    }

    @Test
    void testEquals_WithDifferentValues_ShouldReturnFalse() {
        // Given
        AuthInfoBean authInfo1 = new AuthInfoBean();
        authInfo1.setClientkey("key1");
        authInfo1.setClientsecret("secret1");
        
        AuthInfoBean authInfo2 = new AuthInfoBean();
        authInfo2.setClientkey("key2");
        authInfo2.setClientsecret("secret2");

        // When & Then
        assertThat(authInfo1).isNotEqualTo(authInfo2);
    }

    @Test
    void testEquals_WithSameInstance_ShouldReturnTrue() {
        // Given
        AuthInfoBean authInfo = new AuthInfoBean();
        authInfo.setClientkey("key1");
        authInfo.setClientsecret("secret1");

        // When & Then
        assertThat(authInfo).isEqualTo(authInfo);
    }

    @Test
    void testEquals_WithNull_ShouldReturnFalse() {
        // Given
        AuthInfoBean authInfo = new AuthInfoBean();
        authInfo.setClientkey("key1");
        authInfo.setClientsecret("secret1");

        // When & Then
        assertThat(authInfo).isNotEqualTo(null);
    }

    @Test
    void testEquals_WithDifferentClass_ShouldReturnFalse() {
        // Given
        AuthInfoBean authInfo = new AuthInfoBean();
        authInfo.setClientkey("key1");
        authInfo.setClientsecret("secret1");
        String otherObject = "not-auth-info";

        // When & Then
        assertThat(authInfo).isNotEqualTo(otherObject);
    }

    @Test
    void testHashCode_WithSameValues_ShouldReturnSameHashCode() {
        // Given
        AuthInfoBean authInfo1 = new AuthInfoBean();
        authInfo1.setClientkey("key1");
        authInfo1.setClientsecret("secret1");
        
        AuthInfoBean authInfo2 = new AuthInfoBean();
        authInfo2.setClientkey("key1");
        authInfo2.setClientsecret("secret1");

        // When & Then
        assertThat(authInfo1.hashCode()).isEqualTo(authInfo2.hashCode());
    }

    @Test
    void testHashCode_WithDifferentValues_ShouldReturnDifferentHashCode() {
        // Given
        AuthInfoBean authInfo1 = new AuthInfoBean();
        authInfo1.setClientkey("key1");
        authInfo1.setClientsecret("secret1");
        
        AuthInfoBean authInfo2 = new AuthInfoBean();
        authInfo2.setClientkey("key2");
        authInfo2.setClientsecret("secret2");

        // When & Then
        assertThat(authInfo1.hashCode()).isNotEqualTo(authInfo2.hashCode());
    }

    @Test
    void testToString_ShouldContainFieldNames() {
        // Given
        AuthInfoBean authInfo = new AuthInfoBean();
        authInfo.setClientkey("test-key");
        authInfo.setClientsecret("test-secret");

        // When
        String toString = authInfo.toString();

        // Then
        assertThat(toString).contains("clientkey");
        assertThat(toString).contains("clientsecret");
        assertThat(toString).contains("test-key");
        assertThat(toString).contains("test-secret");
    }

    @Test
    void testToString_WithNullValues_ShouldHandleGracefully() {
        // Given
        AuthInfoBean authInfo = new AuthInfoBean();

        // When
        String toString = authInfo.toString();

        // Then
        assertThat(toString).contains("null");
    }
}
