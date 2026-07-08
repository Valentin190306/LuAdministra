package com.luadministra.exception;

import org.eclipse.jdt.annotation.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), "NO_ENCONTRADO", 404));
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInvalida(SolicitudInvalidaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage(), "SOLICITUD_INVALIDA", 400));
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleStock(StockInsuficienteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage(), "STOCK_INSUFICIENTE", 409));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleValidacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validacion");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(mensaje, "VALIDACION", 400));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleGenerico(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", "ERROR_INTERNO", 500));
    }
}
