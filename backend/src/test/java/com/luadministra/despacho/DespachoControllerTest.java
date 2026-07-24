package com.luadministra.despacho;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luadministra.colaboradora.Colaboradora;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DespachoController.class)
@Import(GlobalExceptionHandler.class)
class DespachoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DespachoService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void listar_retorna200() throws Exception {
        when(service.listar(anyInt(), anyInt(), any(), any(), any(), any()))
                .thenReturn(new com.luadministra.dto.PaginatedResponse<>(List.of(), 0, 50, 0, 0));

        mockMvc.perform(get("/api/despachos"))
                .andExpect(status().isOk());
    }

    @Test
    void obtener_retorna200() throws Exception {
        Colaboradora colab = new Colaboradora();
        colab.setId(1L);
        colab.setNombre("Ana");
        Despacho d = new Despacho();
        d.setId(1L);
        d.setColaboradora(colab);

        when(service.obtener(1L)).thenReturn(DespachoResponse.fromEntity(d));

        mockMvc.perform(get("/api/despachos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colaboradoraNombre").value("Ana"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new RecursoNoEncontradoException("no encontrado"));

        mockMvc.perform(get("/api/despachos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }

    @Test
    void crear_retorna200() throws Exception {
        when(service.crear(any())).thenReturn(new DespachoResponse(1L, 1L, "Ana", LocalDate.now(),
                EstadoDespacho.PENDIENTE, List.of()));

        String body = mapper.writeValueAsString(new DespachoRequest(1L, LocalDate.now(),
                List.of(new LineaDespachoRequest(1L, 5.0))));

        mockMvc.perform(post("/api/despachos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colaboradoraNombre").value("Ana"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400() throws Exception {
        String body = mapper.writeValueAsString(new DespachoRequest(null, null, null));

        mockMvc.perform(post("/api/despachos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tipo").value("VALIDACION"));
    }

    @Test
    void eliminar_retorna204() throws Exception {
        mockMvc.perform(delete("/api/despachos/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }

    @Test
    void stockConsignado_retorna200() throws Exception {
        when(service.stockConsignado(1L)).thenReturn(List.of(
                new StockConsignadoResponse(1L, "Jabón", 10.0, 5.0, 5.0)));

        mockMvc.perform(get("/api/despachos/stock-consignado?colaboradoraId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productoTerminadoNombre").value("Jabón"))
                .andExpect(jsonPath("$[0].cantidadPendiente").value(5.0));
    }

    @Test
    void stockConsignado_cuandoNoExisteColaboradora_retorna404() throws Exception {
        when(service.stockConsignado(99L)).thenThrow(new RecursoNoEncontradoException("no encontrada"));

        mockMvc.perform(get("/api/despachos/stock-consignado?colaboradoraId=99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("NO_ENCONTRADO"));
    }
}
