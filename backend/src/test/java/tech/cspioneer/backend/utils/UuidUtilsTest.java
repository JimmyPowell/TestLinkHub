package tech.cspioneer.backend.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UuidUtilsTest {
    @Test
    void testRandomUuid_FormatAndUniqueness() {
        String uuid1 = UuidUtils.randomUuid();
        String uuid2 = UuidUtils.randomUuid();
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
        assertEquals(36, uuid1.length());
        assertTrue(uuid1.matches("[0-9a-f-]{36}"));
    }

    @Test
    void testRandomUuidWithoutDash_FormatAndUniqueness() {
        String uuid1 = UuidUtils.randomUuidWithoutDash();
        String uuid2 = UuidUtils.randomUuidWithoutDash();
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
        assertEquals(32, uuid1.length());
        assertTrue(uuid1.matches("[0-9a-f]{32}"));
    }
} 