package tech.cspioneer.backend.exception;

public class NewsServiceException extends RuntimeException {
    public NewsServiceException(String message) {
        super(message);
    }
    public NewsServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
