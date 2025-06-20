package tech.cspioneer.backend.utils;

import java.util.UUID;
/**
 *  UUID   .
 */
public final class UuidUtils {
    private UuidUtils() {
    }

    /**
     *   4UUID  .
     *
     * @return  UUID 
     */
    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     *   4UUID  .
     *
     * @return  UUID 
     */
    public static String randomUuidWithoutDash() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}