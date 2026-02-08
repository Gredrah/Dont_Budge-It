package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
    Event e1;
    Event e2;
    Event e3;
    Event e4;
    String notEvent;

    @BeforeEach
    public void setup() {
        e1 = new Event("Test Event");
        e2 = new Event("Test Event");
        e3 = new Event("Test Event 2");
        e4 = null;
        notEvent = "Pretending to be an Event";
    }

    @Test
    public void testConstructor() {
        assertEquals(Calendar.getInstance().getTime(), e1.getDate());
        assertEquals("Test Event", e1.getDescription());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEquals() {
        assertTrue(e1.equals(e2));
        assertFalse(e1.equals(e3));
        assertFalse(e1.equals(e4));
        assertFalse(e1.equals(notEvent));
    }

    @Test
    public void testHashcode() {
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals(e1.getDate().toString() + "\n" + e1.getDescription(), e1.toString());
    }
}
