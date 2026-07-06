package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an administrative action (e.g. deleting a user via the
 * SYSADMIN/ADMIN endpoint) is attempted by a user against their own
 * account. Such actions must go through the regular self-service
 * endpoints instead (e.g. {@code DELETE /auth/users}).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SelfActionNotAllowedException extends RuntimeException {

    public SelfActionNotAllowedException() {
        super();
    }

    public SelfActionNotAllowedException(String message) {
        super(message);
    }
}
