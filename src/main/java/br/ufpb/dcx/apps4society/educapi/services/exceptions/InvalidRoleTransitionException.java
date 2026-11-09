package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a role change (promote/demote) is requested for a User that
 * is not in a valid starting state for that transition — e.g. trying to
 * demote a CLIENTE (who is not ADMIN) or a SYSADMIN (whose role cannot be
 * changed through this action).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidRoleTransitionException extends RuntimeException {

    public InvalidRoleTransitionException() {
        super();
    }

    public InvalidRoleTransitionException(String message) {
        super(message);
    }
}
