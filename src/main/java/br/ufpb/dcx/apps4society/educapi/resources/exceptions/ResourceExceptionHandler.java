package br.ufpb.dcx.apps4society.educapi.resources.exceptions;

import br.ufpb.dcx.apps4society.educapi.services.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class ResourceExceptionHandler {
	
	@ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<StandardError> objectNotFound(ObjectNotFoundException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.NOT_FOUND.value(), e.getMessage(), System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
	}
	
	@ExceptionHandler(DataIntegrityException.class)
	public ResponseEntity<StandardError> dataIntegrity(DataIntegrityException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.BAD_REQUEST.value(), e.getMessage(), System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<StandardError> validation(MethodArgumentNotValidException e, HttpServletRequest request){
		
		ValidationError err = new ValidationError(HttpStatus.BAD_REQUEST.value(), "Validation error", System.currentTimeMillis(), request.getRequestURI());
		for(FieldError x : e.getBindingResult().getFieldErrors()) {
			err.addError(x.getField(), x.getDefaultMessage());
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<StandardError> constraintViolationException(ConstraintViolationException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.BAD_REQUEST.value(), e.getMessage() ,System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}

	@ExceptionHandler(JpaObjectRetrievalFailureException.class)
	public ResponseEntity<StandardError> entityNotFoundException(JpaObjectRetrievalFailureException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.BAD_REQUEST.value(), e.getMessage() ,System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}

	@ExceptionHandler(ChallengeAlreadyExistsException.class)
	public ResponseEntity<StandardError> challengeAlreadyExistsException(ChallengeAlreadyExistsException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.CONFLICT.value(), e.getMessage() ,System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	@ExceptionHandler(ContextAlreadyExistsException.class)
	public ResponseEntity<StandardError> contextAlreadyExistsException(ContextAlreadyExistsException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.CONFLICT.value(), e.getMessage() ,System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<StandardError> userAlreadyExistsException(UserAlreadyExistsException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.CONFLICT.value(), e.getMessage() ,System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	@ExceptionHandler(InvalidUserException.class)
	public ResponseEntity<StandardError> invalidUserException(InvalidUserException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.UNAUTHORIZED.value(), e.getMessage() ,System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
	}
}
