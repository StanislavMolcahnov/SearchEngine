package main.exceptions;

public class BadLemmaException extends Exception {
    private String message;

    public BadLemmaException(String message) {
        super(message);
    }
}
