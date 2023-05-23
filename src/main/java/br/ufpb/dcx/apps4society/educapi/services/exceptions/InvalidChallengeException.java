package br.ufpb.dcx.apps4society.educapi.services.exceptions;

public class InvalidChallengeException extends Exception{


    public InvalidChallengeException() {
        super();
    }

    public InvalidChallengeException(String word) {
        super(word);
    }
}
