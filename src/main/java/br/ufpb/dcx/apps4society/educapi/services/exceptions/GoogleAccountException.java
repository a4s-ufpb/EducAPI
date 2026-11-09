package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class GoogleAccountException extends RuntimeException {

    public GoogleAccountException() {
        super();
    }

    public GoogleAccountException(String message) {
        super(message);
    }
}
