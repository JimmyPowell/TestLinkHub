package tech.cspioneer.backend.exception;

public class LessonServiceException extends RuntimeException {
    public LessonServiceException(String message) {
        super(message);
    }
    public LessonServiceException(String message, Throwable cause) {
        super(message, cause);
    }
} 