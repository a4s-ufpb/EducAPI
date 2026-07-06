package br.ufpb.dcx.apps4society.educapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the requested retention period for a log purge is too short
 * (below the safety floor) to prevent an accidental mass-deletion of the
 * audit trail.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRetentionPeriodException extends RuntimeException {

    public InvalidRetentionPeriodException() {
        super();
    }

    public InvalidRetentionPeriodException(String message) {
        super(message);
    }
}
