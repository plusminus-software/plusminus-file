package software.plusminus.file.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
    }

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public FileNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
