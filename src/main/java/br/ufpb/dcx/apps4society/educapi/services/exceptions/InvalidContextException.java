package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidContextException extends RuntimeException{

    public InvalidContextException() {
        super();
    }
    public InvalidContextException(String s) {
        super(s);
    }
}
