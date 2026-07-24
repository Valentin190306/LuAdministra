package com.luadministra.rendicion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RendicionController.class)
@Import(GlobalExceptionHandler.class)
class RendicionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RendicionService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void listarPorDespacho_retorna200() throws Exception {
        when(service.listarPorDespacho(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/despachos/1/rendiciones"))
                .andExpect(status().isOk());
    }

    @Test
    void crear_retorna200() throws Exception {
        when(service.crear(any())).thenReturn(
                new RendicionResponse(1L, 1L, 1L, "Ana", 500.0, LocalDate.now(), List.of()));

        String body = mapper.writeValueAsString(
                new RendicionRequest(1L, List.of(new LineaRendicionRequest(1L, 5.0, 0.0)), 500.0, LocalDate.now()));

        mockMvc.perform(post("/api/rendiciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoEntregado").value(500.0));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new RendicionRequest(null, null, null, null));

        mockMvc.perform(post("/api/rendiciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_cuandoDespachoNoExiste_retorna404() throws Exception {
        when(service.crear(any())).thenThrow(new RecursoNoEncontradoException("no encontrado"));

        String body = mapper.writeValueAsString(
                new RendicionRequest(99L, List.of(new LineaRendicionRequest(1L, 0.0, 0.0)), 0.0, LocalDate.now()));

        mockMvc.perform(post("/api/rendiciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_cuandoSuperaDisponible_retorna400() throws Exception {
        when(service.crear(any())).thenThrow(new SolicitudInvalidaException("supera el disponible"));

        String body = mapper.writeValueAsString(
                new RendicionRequest(1L, List.of(new LineaRendicionRequest(1L, 99.0, 0.0)), 0.0, LocalDate.now()));

        mockMvc.perform(post("/api/rendiciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("SOLICITUD_INVALIDA"));
    }
}
