package com.luadministra.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNoEncontrado_retorna404() {
        ResponseEntity<ErrorResponse> res = handler.handleNoEncontrado(
                new RecursoNoEncontradoException("Materia prima no encontrada"));

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("NO_ENCONTRADO", res.getBody().tipo());
        assertEquals(404, res.getBody().codigo());
    }

    @Test
    void handleInvalida_retorna400() {
        ResponseEntity<ErrorResponse> res = handler.handleInvalida(
                new SolicitudInvalidaException("Datos invalidos"));

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("SOLICITUD_INVALIDA", res.getBody().tipo());
    }

    @Test
    void handleStock_retorna409() {
        ResponseEntity<ErrorResponse> res = handler.handleStock(
                new StockInsuficienteException("Stock insuficiente"));

        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("STOCK_INSUFICIENTE", res.getBody().tipo());
    }

    @Test
    void handleValidacion_retorna400() {
        BindException bindException = new BindException(new Object(), "test");
        bindException.addError(new FieldError("test", "nombre", "no debe estar vacío"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindException);

        ResponseEntity<ErrorResponse> res = handler.handleValidacion(ex);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("VALIDACION", res.getBody().tipo());
        assertEquals(400, res.getBody().codigo());
    }

    @Test
    void handleGenerico_retorna500() {
        ResponseEntity<ErrorResponse> res = handler.handleGenerico(
                new RuntimeException("error inesperado"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("ERROR_INTERNO", res.getBody().tipo());
        assertEquals("Error interno del servidor", res.getBody().mensaje());
    }
}
