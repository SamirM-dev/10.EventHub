package com.example.eventhub.exception;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e,HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExist(ResourceAlreadyExistsException e, HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentValid(MethodArgumentNotValidException e,HttpServletRequest request){
        List<ErrorResponse.ErrorField> errorFields = e.getBindingResult().getFieldErrors()
                .stream().map(field->new ErrorResponse.ErrorField(field.getObjectName(), field.getDefaultMessage())).toList();
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation Failed",request.getRequestURI()
        );
        response.setErrorFields(errorFields);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e,HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e,HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException e,HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e,HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(NoConfirmedBookingException.class)
    public ResponseEntity<ErrorResponse> handleNoConfirmedBooking(NoConfirmedBookingException e,HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage(),request.getRequestURI()
        );
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e,HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Server has some problems...",request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(response);
    }
}
