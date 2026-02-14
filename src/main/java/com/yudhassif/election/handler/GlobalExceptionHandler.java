package com.yudhassif.election.handler;
import com.yudhassif.election.exception.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.relation.RoleNotFoundException;
import java.util.HashMap;
import java.util.Map;

/*Global Exception handler it controls all exception handling in each controller
So instead of handle exception in each controller in any project like 5 controller require 5 exception handling
so if we create Global exception handler it generalizes all controller
*/
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleException(IllegalStateException  exception){
        return ResponseEntity
                .badRequest()
                .body(exception.getMessage());

    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleException(EntityNotFoundException  exception){
        return ResponseEntity
                .notFound()
                .build();
    }

    @ExceptionHandler(UserRoleNotFoundException.class)
    public ResponseEntity<String> handleException(UserRoleNotFoundException exception){
        return ResponseEntity
                .badRequest()
                .build();
    }
    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<String> handleException(MessageNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMsg());
    }




    //here is the code used to handle validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgNotValidException(MethodArgumentNotValidException exp, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String fieldName = "object_error"; // Default key for a non-field error
                    if (error instanceof FieldError) {
                        fieldName = ((FieldError) error).getField();
                    } else {
                        // For global object errors, use a generic key
                        fieldName = ((ObjectError) error).getObjectName();
                    }

                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleException(InvalidTokenException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMsg());
    }
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handleException(TokenExpiredException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(PasswordNotFoundException.class)
    public ResponseEntity<String> handleException(PasswordNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<String> handleException(StudentNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<String> handleException(DepartmentNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMsg());
    }
    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<String> handleException(CourseNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMsg());
    }
    @ExceptionHandler(AlreadyActivatedException.class)
    public ResponseEntity<String> handleException(AlreadyActivatedException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMsg());
    }

    @ExceptionHandler(ElectionNotFoundException.class)
    public ResponseEntity<String> handleException(ElectionNotFoundException exception){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMsg());
    }
}

