package drr.regulation.common.util.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringLengthImplTest {

    @Test
    void shouldReturnLength3() {
        StringLength func = new StringLengthImpl();
        Integer strLength = func.evaluate("foo");

        assertEquals(3, strLength);
    }

    @Test
    void shouldReturnLength0() {
        StringLength func = new StringLengthImpl();
        Integer strLength = func.evaluate(null);

        assertEquals(0, strLength);
    }
}