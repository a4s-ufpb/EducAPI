package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a SYSADMIN attempts to promote a User that already has
 * ADMIN or SYSADMIN privileges (only CLIENTE users can be promoted).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyPromotedException extends RuntimeException {

    public UserAlreadyPromotedException() {
        super();
    }

    public UserAlreadyPromotedException(String message) {
        super(message);
    }
}
