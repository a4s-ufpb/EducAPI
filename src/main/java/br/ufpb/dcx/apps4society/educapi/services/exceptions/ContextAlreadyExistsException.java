package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ContextAlreadyExistsException extends RuntimeException{

    public ContextAlreadyExistsException(){
        super();
    }

    public ContextAlreadyExistsException(String message){
        super(message);
    }
}
