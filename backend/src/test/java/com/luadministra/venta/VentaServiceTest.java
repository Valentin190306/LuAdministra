package com.luadministra.venta;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoTerminadoRepository productoTerminadoRepository;

    @InjectMocks
    private VentaService service;

    @Test
    void crear_descruentaStockPT() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(10.0);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));

        Venta saved = new Venta();
        saved.setId(1L);
        saved.setProductoTerminado(pt);
        saved.setCantidad(3.0);
        when(ventaRepository.save(any())).thenReturn(saved);

        VentaRequest request = new VentaRequest(1L, LocalDate.now(), 3.0);
        service.crear(request);

        assertEquals(7.0, pt.getStockActual());
        verify(productoTerminadoRepository).save(pt);
    }

    @Test
    void crear_cuandoStockInsuficiente_lanzaExcepcion() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(2.0);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));

        VentaRequest request = new VentaRequest(1L, LocalDate.now(), 5.0);
        assertThrows(StockInsuficienteException.class, () -> service.crear(request));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void crear_cuandoProductoNoExiste_lanzaExcepcion() {
        when(productoTerminadoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(new VentaRequest(99L, LocalDate.now(), 1.0)));
    }

    @Test
    void crear_capturaPrecioUnitario() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(10.0);
        pt.setPrecioVenta(2500.0);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));

        Venta saved = new Venta();
        saved.setId(1L);
        saved.setProductoTerminado(pt);
        saved.setCantidad(3.0);
        saved.setPrecioUnitario(2500.0);
        when(ventaRepository.save(any())).thenReturn(saved);

        VentaRequest request = new VentaRequest(1L, LocalDate.now(), 3.0);
        VentaResponse result = service.crear(request);

        assertEquals(2500.0, result.precioUnitario());
    }

    @Test
    void eliminar_revierteStockPT() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(7.0);

        Venta venta = new Venta();
        venta.setId(1L);
        venta.setProductoTerminado(pt);
        venta.setCantidad(3.0);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        service.eliminar(1L);

        assertEquals(10.0, pt.getStockActual());
        verify(productoTerminadoRepository).save(pt);
        verify(ventaRepository).delete(venta);
    }
}
