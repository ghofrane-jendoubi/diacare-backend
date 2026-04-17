package tn.esprit.spring.diacarebackend.exception;

public class FeedbackAlreadyExistsException extends RuntimeException {
    public FeedbackAlreadyExistsException(String message) {
        super(message);
    }
}
