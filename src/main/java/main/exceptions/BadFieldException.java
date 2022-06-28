package main.exceptions;

public class BadFieldException extends Exception {

    private String message;

    public BadFieldException(String message) {
        super(message);
    }
}
