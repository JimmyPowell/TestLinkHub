package tech.cspioneer.backend.utils;

import java.util.Random;

public class CodeGeneratorUtil {

    private static final Random RANDOM = new Random();

    /**
     * Generates a 6-digit numeric verification code.
     * @return A string representing the 6-digit code.
     */
    public static String generateSixDigitCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
