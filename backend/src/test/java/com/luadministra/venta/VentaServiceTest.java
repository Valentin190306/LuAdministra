package com.luadministra.venta;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaService service;

    @Test
    void listar_retornaPagina() {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setFecha(LocalDate.now());

        Page<Venta> page = new PageImpl<>(List.of(venta));
        when(ventaRepository.findAll(any(PageRequest.class))).thenReturn(page);

        var result = service.listar(0, 50, null, null);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalPages());
    }

    @Test
    void crear_descruentaStockPT() {
        Producto pt = new Producto();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(10.0);
        pt.setPrecioVenta(2000.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(pt));

        Venta saved = new Venta();
        saved.setId(1L);
        saved.setFecha(LocalDate.now());
        LineaVenta linea = new LineaVenta();
        linea.setId(1L);
        linea.setVenta(saved);
        linea.setProducto(pt);
        linea.setCantidad(3.0);
        linea.setPrecioUnitario(2000.0);
        saved.setLineas(List.of(linea));

        when(ventaRepository.save(any())).thenReturn(saved);

        VentaRequest request = new VentaRequest(LocalDate.now(), List.of(new LineaVentaRequest(1L, 3.0, null)));
        VentaResponse result = service.crear(request);

        assertEquals(7.0, pt.getStockActual());
        assertEquals(1, result.lineas().size());
        verify(productoRepository).save(pt);
    }

    @Test
    void crear_cuandoStockInsuficiente_lanzaExcepcion() {
        Producto pt = new Producto();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(2.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(pt));

        VentaRequest request = new VentaRequest(LocalDate.now(), List.of(new LineaVentaRequest(1L, 5.0, null)));
        assertThrows(StockInsuficienteException.class, () -> service.crear(request));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void crear_cuandoProductoNoExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(new VentaRequest(LocalDate.now(), List.of(new LineaVentaRequest(99L, 1.0, null)))));
    }

    @Test
    void crear_capturaPrecioUnitario() {
        Producto pt = new Producto();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(10.0);
        pt.setPrecioVenta(2500.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(pt));

        Venta saved = new Venta();
        saved.setId(1L);
        saved.setFecha(LocalDate.now());
        LineaVenta linea = new LineaVenta();
        linea.setId(1L);
        linea.setVenta(saved);
        linea.setProducto(pt);
        linea.setCantidad(3.0);
        linea.setPrecioUnitario(2500.0);
        saved.setLineas(List.of(linea));

        when(ventaRepository.save(any())).thenReturn(saved);

        VentaRequest request = new VentaRequest(LocalDate.now(), List.of(new LineaVentaRequest(1L, 3.0, null)));
        VentaResponse result = service.crear(request);

        assertEquals(2500.0, result.lineas().get(0).precioUnitario());
    }

    @Test
    void crear_multiplesLineas() {
        Producto pt1 = new Producto();
        pt1.setId(1L);
        pt1.setNombre("Shampoo");
        pt1.setStockActual(10.0);
        pt1.setPrecioVenta(2500.0);

        Producto pt2 = new Producto();
        pt2.setId(2L);
        pt2.setNombre("Jabón");
        pt2.setStockActual(20.0);
        pt2.setPrecioVenta(1500.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(pt1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(pt2));

        Venta saved = new Venta();
        saved.setId(1L);
        saved.setFecha(LocalDate.now());
        LineaVenta l1 = new LineaVenta();
        l1.setId(1L); l1.setVenta(saved); l1.setProducto(pt1); l1.setCantidad(3.0); l1.setPrecioUnitario(2500.0);
        LineaVenta l2 = new LineaVenta();
        l2.setId(2L); l2.setVenta(saved); l2.setProducto(pt2); l2.setCantidad(2.0); l2.setPrecioUnitario(1500.0);
        saved.setLineas(List.of(l1, l2));

        when(ventaRepository.save(any())).thenReturn(saved);

        VentaRequest request = new VentaRequest(LocalDate.now(), List.of(
                new LineaVentaRequest(1L, 3.0, null),
                new LineaVentaRequest(2L, 2.0, null)
        ));
        VentaResponse result = service.crear(request);

        assertEquals(7.0, pt1.getStockActual());
        assertEquals(18.0, pt2.getStockActual());
        assertEquals(2, result.lineas().size());
        assertEquals(10500.0, result.total());
    }

    @Test
    void eliminar_revierteStockPT() {
        Producto pt = new Producto();
        pt.setId(1L);
        pt.setNombre("Shampoo");
        pt.setStockActual(7.0);

        Venta venta = new Venta();
        venta.setId(1L);
        LineaVenta linea = new LineaVenta();
        linea.setId(1L);
        linea.setVenta(venta);
        linea.setProducto(pt);
        linea.setCantidad(3.0);
        linea.setPrecioUnitario(2000.0);
        venta.setLineas(List.of(linea));

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        service.eliminar(1L);

        assertEquals(10.0, pt.getStockActual());
        verify(productoRepository).save(pt);
        verify(ventaRepository).delete(venta);
    }
}
