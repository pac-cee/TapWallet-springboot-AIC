package rw.ac.auca.tapwallet.exception;

/** Thrown when a uniqueness rule would be broken (email, phone, code, one wallet per user). Mapped to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
