package tech.cspioneer.backend.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorUtilTest {
    @Test
    void testGenerateSixDigitCode_FormatAndRange() {
        for (int i = 0; i < 100; i++) {
            String code = CodeGeneratorUtil.generateSixDigitCode();
            assertNotNull(code);
            assertEquals(6, code.length());
            assertTrue(code.matches("\\d{6}"));
            int num = Integer.parseInt(code);
            assertTrue(num >= 100000 && num <= 999999);
        }
    }
} 