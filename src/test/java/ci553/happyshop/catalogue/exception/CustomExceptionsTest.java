package ci553.happyshop.catalogue.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionsTest {

    @Test
    void underMinimumPaymentException_holdsMessage() {
        UnderMinimumPaymentException ex = new UnderMinimumPaymentException("Too low");
        assertEquals("Too low", ex.getMessage());
        assertTrue(ex instanceof Exception);
    }

    @Test
    void excessiveOrderQuantityException_holdsMessage() {
        ExcessiveOrderQuantityException ex = new ExcessiveOrderQuantityException("Too many");
        assertEquals("Too many", ex.getMessage());
        assertTrue(ex instanceof Exception);
    }
}
