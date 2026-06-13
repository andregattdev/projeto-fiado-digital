package com.app.Projeto.fiado.digital.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_deveRetornar404() {
        ResponseEntity<ErrorResponse> response = handler
                .handleNotFound(new ResourceNotFoundException("Cliente não encontrado"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Cliente não encontrado", response.getBody().message());
    }

    @Test
    void handleBusiness_deveRetornar400() {
        ResponseEntity<ErrorResponse> response = handler
                .handleBusiness(new BusinessException("Valor inválido"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
    }
}
