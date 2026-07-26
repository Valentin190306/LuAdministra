package com.luadministra.dashboard;

import com.luadministra.producto.ProductoService;
import com.luadministra.exception.GlobalExceptionHandler;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.lote.LoteRepository;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
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
    private ProductoRepository productoRepository;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private LoteRepository loteRepository;

    @Test
    void stock_retornaDashboard() throws Exception {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setNombre("Aceite");
        mp.setUnidadMedida("ml");
        mp.setStockActual(100.0);
        mp.setStockMinimo(50.0);

        Producto pt = new Producto();
        pt.setId(1L);
        pt.setNombre("Jabón");
        pt.setStockActual(20.0);
        pt.setStockMinimo(10.0);
        pt.setPrecioVenta(100.0);

        when(materiaPrimaRepository.findAll()).thenReturn(List.of(mp));
        when(productoRepository.findAll()).thenReturn(List.of(pt));
        when(productoService.calcularStockConsignado(1L)).thenReturn(5.0);
        when(loteRepository.findByDiasVigenciaIsNotNull()).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiasPrimas[0].nombre").value("Aceite"))
                .andExpect(jsonPath("$.productos[0].nombre").value("Jabón"))
                .andExpect(jsonPath("$.productos[0].stockConsignado").value(5.0))
                .andExpect(jsonPath("$.alertasMP").isArray())
                .andExpect(jsonPath("$.alertasProductos").isArray());
    }

    @Test
    void stock_conStockMinimoNull_noAlerta() throws Exception {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setNombre("Aceite");
        mp.setUnidadMedida("ml");
        mp.setStockActual(100.0);

        when(materiaPrimaRepository.findAll()).thenReturn(List.of(mp));
        when(productoRepository.findAll()).thenReturn(List.of());
        when(productoService.calcularStockConsignado(anyLong())).thenReturn(0.0);
        when(loteRepository.findByDiasVigenciaIsNotNull()).thenReturn(List.of());

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
        when(productoRepository.findAll()).thenReturn(List.of());
        when(productoService.calcularStockConsignado(anyLong())).thenReturn(0.0);
        when(loteRepository.findByDiasVigenciaIsNotNull()).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertasMP[0].nombre").value("Aceite"));
    }

    private static long anyLong() {
        return org.mockito.ArgumentMatchers.anyLong();
    }
}
