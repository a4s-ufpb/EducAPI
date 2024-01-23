package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidUserException extends RuntimeException{

    public InvalidUserException() {
        super();
    }

    public InvalidUserException(String s) {
        super(s);
    }
}