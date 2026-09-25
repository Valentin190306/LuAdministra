package com.luadministra.consignacion;

import com.luadministra.consignatario.Consignatario;
import com.luadministra.consignatario.ConsignatarioRepository;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.rendicion.RendicionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsignacionServiceTest {

    @Mock
    private ConsignacionRepository repository;

    @Mock
    private ConsignatarioRepository consignatarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RendicionRepository rendicionRepository;

    @InjectMocks
    private ConsignacionService service;

    @Test
    void eliminar_sinRendiciones_elimina() {
        Consignacion consignacion = new Consignacion();
        consignacion.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(consignacion));
        when(rendicionRepository.countByConsignacionId(1L)).thenReturn(0L);

        service.eliminar(1L);

        verify(repository).delete(consignacion);
    }

    @Test
    void eliminar_conRendiciones_lanzaExcepcion() {
        Consignacion consignacion = new Consignacion();
        consignacion.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(consignacion));
        when(rendicionRepository.countByConsignacionId(1L)).thenReturn(2L);

        assertThrows(SolicitudInvalidaException.class, () -> service.eliminar(1L));
        verify(repository, never()).delete(any());
    }

    @Test
    void eliminar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
    }

    private Producto producto(Long id, String nombre, double stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setStockActual(stock);
        p.setPrecioVenta(100.0);
        return p;
    }

    private ConsignacionRequest request(Long consignatarioId, LineaConsignacionRequest... lineas) {
        return new ConsignacionRequest(consignatarioId, LocalDate.now(), List.of(lineas));
    }

    private void mockConsignatario() {
        Consignatario c = new Consignatario();
        c.setId(1L);
        c.setNombre("Consignataria");
        when(consignatarioRepository.findById(1L)).thenReturn(Optional.of(c));
    }

    @Test
    void crear_cuandoStockSuficiente_crea() {
        mockConsignatario();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Jabon", 10.0)));
        when(repository.findLineasByProductoIdAndEstadoNot(1L, EstadoConsignacion.RENDIDO_TOTAL)).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConsignacionResponse result = service.crear(request(1L, new LineaConsignacionRequest(1L, 5.0)));

        assertEquals(1, result.productos().size());
        assertEquals(5.0, result.productos().get(0).cantidad());
        verify(repository).save(any());
    }

    @Test
    void crear_cuandoStockInsuficiente_lanzaExcepcion() {
        mockConsignatario();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Jabon", 10.0)));
        LineaConsignacion existente = new LineaConsignacion();
        existente.setCantidad(8.0);
        when(repository.findLineasByProductoIdAndEstadoNot(1L, EstadoConsignacion.RENDIDO_TOTAL))
                .thenReturn(List.of(existente));

        assertThrows(StockInsuficienteException.class,
                () -> service.crear(request(1L, new LineaConsignacionRequest(1L, 5.0))));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_repiteProducto_sumaYValida() {
        mockConsignatario();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto(1L, "Jabon", 5.0)));
        when(repository.findLineasByProductoIdAndEstadoNot(1L, EstadoConsignacion.RENDIDO_TOTAL)).thenReturn(List.of());

        assertThrows(StockInsuficienteException.class, () -> service.crear(request(1L,
                new LineaConsignacionRequest(1L, 3.0),
                new LineaConsignacionRequest(1L, 3.0))));
        verify(repository, never()).save(any());
    }

    @Test
    void crear_cuandoCantidadCero_lanzaExcepcion() {
        mockConsignatario();

        assertThrows(SolicitudInvalidaException.class,
                () -> service.crear(request(1L, new LineaConsignacionRequest(1L, 0.0))));
        verify(repository, never()).save(any());
    }
}
