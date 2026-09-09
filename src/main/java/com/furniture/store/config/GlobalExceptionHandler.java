package com.furniture.store.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
            IllegalArgumentException ex,
            Model model) {

        model.addAttribute(
                "errorTitle",
                "Invalid Request"
        );

        model.addAttribute(
                "errorMessage",
                ex.getMessage()
        );

        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(
            Exception ex,
            Model model) {

        model.addAttribute(
                "errorTitle",
                "Something Went Wrong"
        );

        model.addAttribute(
                "errorMessage",
                "We could not process your request right now. Please try again."
        );

        return "error";
    }
}