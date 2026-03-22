package app.product.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    // ── isBlank tests ─────────────────────────────────────────────────────────

    @Test
    void testIsBlank_emptyString_returnsTrue() {
        assertTrue(ValidationUtil.isBlank(""));
    }

    @Test
    void testIsBlank_nullValue_returnsTrue() {
        assertTrue(ValidationUtil.isBlank(null));
    }

    @Test
    void testIsBlank_validName_returnsFalse() {
        assertFalse(ValidationUtil.isBlank("Jacket"));
    }

    // ── isPositiveDouble tests ────────────────────────────────────────────────

    @Test
    void testIsPositiveDouble_negativePrice_returnsFalse() {
        assertFalse(ValidationUtil.isPositiveDouble("-5.00"));
    }

    @Test
    void testIsPositiveDouble_validPrice_returnsTrue() {
        assertTrue(ValidationUtil.isPositiveDouble("59.99"));
    }

    @Test
    void testIsPositiveDouble_zeroPrice_returnsFalse() {
        assertFalse(ValidationUtil.isPositiveDouble("0"));
    }

    // ── isNonNegativeInt tests ────────────────────────────────────────────────

    @Test
    void testIsNonNegativeInt_negativeStock_returnsFalse() {
        assertFalse(ValidationUtil.isNonNegativeInt("-2"));
    }

    @Test
    void testIsNonNegativeInt_validStock_returnsTrue() {
        assertTrue(ValidationUtil.isNonNegativeInt("10"));
    }

    @Test
    void testIsNonNegativeInt_zeroStock_returnsTrue() {
        assertTrue(ValidationUtil.isNonNegativeInt("0"));
    }
}