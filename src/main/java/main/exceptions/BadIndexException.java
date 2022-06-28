package main.exceptions;

public class BadIndexException extends Exception {

    private String message;

    public BadIndexException(String message) {
        super(message);
    }
}
