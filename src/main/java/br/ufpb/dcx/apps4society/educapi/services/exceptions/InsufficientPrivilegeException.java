package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a User attempts an administrative action that their Role
 * does not have enough privilege for — e.g. an ADMIN (rather than
 * SYSADMIN) trying to promote a user or delete another ADMIN account.
 *
 * This is distinct from {@link InvalidUserException}, which represents
 * authentication problems (invalid/missing token) or plain ownership
 * violations on regular content; this exception is specifically about
 * the Role hierarchy on admin/sysadmin-only actions.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPrivilegeException extends RuntimeException {

    public InsufficientPrivilegeException() {
        super();
    }

    public InsufficientPrivilegeException(String message) {
        super(message);
    }
}
