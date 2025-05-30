package drr.regulation.common.util.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringContainsImplTest {

    public static final String REGEX_PATTERN = "[A-Z]{2}(-[A-Z]{3})?";

    @Test
    void shouldMatchFullStringWithOptionalPart() {
        StringContains func = new StringContainsImpl();
        boolean result = func.evaluate("ZZ-ABC", REGEX_PATTERN);

        assertTrue(result);
    }

    @Test
    void shouldMatchString() {
        StringContains func = new StringContainsImpl();
        boolean result = func.evaluate("ZZ", REGEX_PATTERN);

        assertTrue(result);
    }

    @Test
    void shouldReturnNotMatchTooLong() {
        StringContains func = new StringContainsImpl();
        boolean result = func.evaluate("ZZZ", REGEX_PATTERN);

        assertFalse(result);
    }

    @Test
    void shouldReturnNotMatchOptionalPartTooShort() {
        StringContains func = new StringContainsImpl();
        boolean result = func.evaluate("ZZ-AB", REGEX_PATTERN);

        assertFalse(result);
    }

    @Test
    void shouldNotMatchWrongSpecialChar() {
        StringContains func = new StringContainsImpl();
        boolean result = func.evaluate("ZZ_ABC", REGEX_PATTERN);

        assertFalse(result);
    }

    @Test
    void shouldHandleEmpty() {
        StringContains func = new StringContainsImpl();
        boolean result = func.evaluate(null, REGEX_PATTERN);

        assertFalse(result);
    }
}