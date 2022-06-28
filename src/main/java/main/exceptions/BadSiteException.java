package main.exceptions;

public class BadSiteException extends Exception {

    private String message;

    public BadSiteException(String message) {
        super(message);
    }
}
