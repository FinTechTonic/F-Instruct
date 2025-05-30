package drr.regulation.common.util.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubStringImplTest {

    @Test
    void shouldReturnShortenedString() {
        SubString func = new SubStringImpl();
        String text = "This is a very long string";
        int desiredLength = 10;

        String result = func.doEvaluate(text, 1, desiredLength);

        assertEquals("This is a ", result);
    }

    @Test
    void shouldReturnShortenedStringWithNullStartIndex() {
        SubString func = new SubStringImpl();
        String text = "This is a very long string";
        int desiredLength = 10;

        String result = func.doEvaluate(text, null, desiredLength);

        assertEquals("This is a ", result);
    }

    @Test
    void shouldReturnShortenedStringWithStartIndex() {
        SubString func = new SubStringImpl();
        String text = "This is a very long string";
        int desiredLength = 14;

        String result = func.doEvaluate(text, 6, desiredLength);

        assertEquals("is a very long", result);
        assertEquals(desiredLength, result.length());
    }

    @Test
    void shouldReturnSameStringIfLengthNotExceeded() {
        SubString func = new SubStringImpl();
        String text = "Short";
        int desiredLength = 10;

        String result = func.doEvaluate(text, 1, desiredLength);

        assertEquals("Short", result);
        assertEquals(5, result.length());
    }

    @Test
    void shouldReturnEmptyStringIfInputIsNull() {
        SubString func = new SubStringImpl();
        int desiredLength = 5;

        String result = func.doEvaluate(null, 1, desiredLength);

        assertNull(result);
    }

    @Test
    void shouldReturnUnchangedStringForInvalidStartIndex() {
        SubString func = new SubStringImpl();
        String text = "This is a very long string";
        int desiredLength = 5;

        String result = func.doEvaluate(text, 0, desiredLength);

        assertEquals(text, result);
    }
}
