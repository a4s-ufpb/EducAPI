package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ChallengeAlreadyExistsException extends RuntimeException{

    public ChallengeAlreadyExistsException() {

        super();
    }

    public ChallengeAlreadyExistsException(String message) {
        super(message);
    }


}
