package main.exceptions;

public class EmptyFrequencyException extends Exception {

    private String message;

    public EmptyFrequencyException(String message) {
        super(message);
    }
}
