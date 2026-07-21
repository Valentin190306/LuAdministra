package com.luadministra.categoriamateriaprima;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaMateriaPrimaController.class)
@Import(GlobalExceptionHandler.class)
class CategoriaMateriaPrimaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaMateriaPrimaService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new CategoriaMateriaPrimaResponse(1L, "Aceites", null, null)));

        mockMvc.perform(get("/api/categorias-materias-primas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Aceites"));
    }

    @Test
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(
                new CategoriaMateriaPrimaResponse(1L, "Aceites", null, null));

        mockMvc.perform(get("/api/categorias-materias-primas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Aceites"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/categorias-materias-primas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new CategoriaMateriaPrimaRequest("", null));

        mockMvc.perform(post("/api/categorias-materias-primas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void crear_retorna201() throws Exception {
        when(service.crear(new CategoriaMateriaPrimaRequest("Aceites", null)))
                .thenReturn(new CategoriaMateriaPrimaResponse(1L, "Aceites", null, null));

        String body = mapper.writeValueAsString(new CategoriaMateriaPrimaRequest("Aceites", null));

        mockMvc.perform(post("/api/categorias-materias-primas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
