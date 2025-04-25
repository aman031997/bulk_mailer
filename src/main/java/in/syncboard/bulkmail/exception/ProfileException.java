package in.syncboard.bulkmail.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProfileException extends RuntimeException {
    private final HttpStatus status;

    public ProfileException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static ProfileException notFound(String message) {
        return new ProfileException(message, HttpStatus.NOT_FOUND);
    }

    public static ProfileException badRequest(String message) {
        return new ProfileException(message, HttpStatus.BAD_REQUEST);
    }

    public static ProfileException conflict(String message) {
        return new ProfileException(message, HttpStatus.CONFLICT);
    }
}
