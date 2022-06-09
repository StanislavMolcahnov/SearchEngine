package main.exceptions;

public class BadPageException extends Exception {
    private String message;

    public BadPageException(String message) {
        super(message);
    }
}
