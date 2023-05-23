package br.ufpb.dcx.apps4society.educapi.services.exceptions;

public class ContextAlreadyExistsException extends Exception{

    public ContextAlreadyExistsException(){
        super();
    }

    public ContextAlreadyExistsException(String message){
        super(message);
    }
}
