package br.ufpb.dcx.apps4society.educapi.resources.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ValidationError extends StandardError {
	private static final long serialVersionUID = 1L;
	
	private List<FieldMessage> errors = new ArrayList<>();
	
	public ValidationError(Integer status, String msg, Long timeStamp,String path) {
		super(status, msg, timeStamp,path);
	}

	public List<FieldMessage> getErros() {
		return errors;
	}

	public void addError(String fieldName, String message) {
		errors.add(new FieldMessage(fieldName, message));
	}

	

}
