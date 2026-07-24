package com.luadministra.dashboard;

import com.luadministra.despacho.DespachoService;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MateriaPrimaRepository materiaPrimaRepository;

    @MockitoBean
    private ProductoTerminadoRepository productoTerminadoRepository;

    @MockitoBean
    private DespachoService despachoService;

    @Test
    void stock_retornaDashboard() throws Exception {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setNombre("Aceite");
        mp.setUnidadMedida("ml");
        mp.setStockActual(100.0);
        mp.setStockMinimo(50.0);

        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setNombre("Jabón");
        pt.setStockActual(20.0);
        pt.setStockMinimo(10.0);
        pt.setPrecioVenta(100.0);

        when(materiaPrimaRepository.findAll()).thenReturn(List.of(mp));
        when(productoTerminadoRepository.findAll()).thenReturn(List.of(pt));
        when(despachoService.calcularStockDespachado(1L)).thenReturn(5.0);

        mockMvc.perform(get("/api/dashboard/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiasPrimas[0].nombre").value("Aceite"))
                .andExpect(jsonPath("$.productosTerminados[0].nombre").value("Jabón"))
                .andExpect(jsonPath("$.productosTerminados[0].stockDespachado").value(5.0))
                .andExpect(jsonPath("$.alertasMP").isArray())
                .andExpect(jsonPath("$.alertasPT").isArray());
    }

    @Test
    void stock_conStockMinimoNull_noAlerta() throws Exception {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setNombre("Aceite");
        mp.setUnidadMedida("ml");
        mp.setStockActual(100.0);

        when(materiaPrimaRepository.findAll()).thenReturn(List.of(mp));
        when(productoTerminadoRepository.findAll()).thenReturn(List.of());
        when(despachoService.calcularStockDespachado(any())).thenReturn(0.0);

        mockMvc.perform(get("/api/dashboard/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertasMP").isEmpty());
    }

    @Test
    void stock_conStockBajo_generaAlerta() throws Exception {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setNombre("Aceite");
        mp.setUnidadMedida("ml");
        mp.setStockActual(3.0);
        mp.setStockMinimo(5.0);

        when(materiaPrimaRepository.findAll()).thenReturn(List.of(mp));
        when(productoTerminadoRepository.findAll()).thenReturn(List.of());
        when(despachoService.calcularStockDespachado(any())).thenReturn(0.0);

        mockMvc.perform(get("/api/dashboard/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertasMP[0].nombre").value("Aceite"));
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
