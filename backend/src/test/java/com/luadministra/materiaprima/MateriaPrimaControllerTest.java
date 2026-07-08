package com.luadministra.materiaprima;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MateriaPrimaController.class)
@Import(GlobalExceptionHandler.class)
class MateriaPrimaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MateriaPrimaService service;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void obtener_retorna200() throws Exception {
        MateriaPrimaResponse resp = new MateriaPrimaResponse(1L, "Test", "gramos", 10.0, 5.0);
        when(service.obtener(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/materias-primas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/materias-primas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new MateriaPrimaRequest("", "", null));

        mockMvc.perform(post("/api/materias-primas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }
}
