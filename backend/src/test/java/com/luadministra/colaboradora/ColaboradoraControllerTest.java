package com.luadministra.colaboradora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ColaboradoraController.class)
@Import(GlobalExceptionHandler.class)
class ColaboradoraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColaboradoraService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new ColaboradoraResponse(1L, "Ana", "11-1234")));

        mockMvc.perform(get("/api/colaboradoras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(new ColaboradoraResponse(1L, "Ana", null));

        mockMvc.perform(get("/api/colaboradoras/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/colaboradoras/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_retorna200() throws Exception {
        when(service.crear(any())).thenReturn(new ColaboradoraResponse(1L, "Ana", "11-1234"));

        String body = mapper.writeValueAsString(new ColaboradoraRequest("Ana", "11-1234"));

        mockMvc.perform(post("/api/colaboradoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new ColaboradoraRequest("", null));

        mockMvc.perform(post("/api/colaboradoras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void actualizar_retorna200() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(new ColaboradoraResponse(1L, "Ana María", null));

        String body = mapper.writeValueAsString(new ColaboradoraRequest("Ana María", null));

        mockMvc.perform(put("/api/colaboradoras/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana María"));
    }

    @Test
    void eliminar_retorna204() throws Exception {
        mockMvc.perform(delete("/api/colaboradoras/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }
}
