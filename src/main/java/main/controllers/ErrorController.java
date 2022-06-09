package main.controllers;

import main.dto.ErrorDto;
import main.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto validationException() {
        ErrorDto errorDto = new ErrorDto();
        errorDto.error = "Введены некорректные данные";
        return errorDto;
    }

    @ExceptionHandler({BadFieldException.class, BadIndexException.class, BadLemmaException.class,
            EmptyFrequencyException.class, BadPageException.class, BadSiteException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDto objectNotFoundInDB(Exception ex) {
        ErrorDto errorDto = new ErrorDto();
        errorDto.error = ex.getMessage();
        return errorDto;
    }
}
