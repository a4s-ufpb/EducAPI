package br.ufpb.dcx.apps4society.educapi.resources.exceptions;

import br.ufpb.dcx.apps4society.educapi.services.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.AccessDeniedException;
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

	@ExceptionHandler(GoogleAccountException.class)
	public ResponseEntity<StandardError> googleAccountException(GoogleAccountException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.FORBIDDEN.value(), e.getMessage() ,System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
	}

	@ExceptionHandler(SelfActionNotAllowedException.class)
	public ResponseEntity<StandardError> selfActionNotAllowedException(SelfActionNotAllowedException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.BAD_REQUEST.value(), e.getMessage(), System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}

	@ExceptionHandler(UserAlreadyPromotedException.class)
	public ResponseEntity<StandardError> userAlreadyPromotedException(UserAlreadyPromotedException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.CONFLICT.value(), e.getMessage(), System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	@ExceptionHandler(InvalidRetentionPeriodException.class)
	public ResponseEntity<StandardError> invalidRetentionPeriodException(InvalidRetentionPeriodException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.BAD_REQUEST.value(), e.getMessage(), System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
	}

	@ExceptionHandler(InvalidRoleTransitionException.class)
	public ResponseEntity<StandardError> invalidRoleTransitionException(InvalidRoleTransitionException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.CONFLICT.value(), e.getMessage(), System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	@ExceptionHandler(InsufficientPrivilegeException.class)
	public ResponseEntity<StandardError> insufficientPrivilegeException(InsufficientPrivilegeException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.FORBIDDEN.value(), e.getMessage(), System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<StandardError> accessDeniedException(AccessDeniedException e, HttpServletRequest request){
		StandardError err = new StandardError(HttpStatus.FORBIDDEN.value(),
				"You do not have permission to perform this action.", System.currentTimeMillis(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
	}
}
