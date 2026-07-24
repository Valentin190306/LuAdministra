package com.luadministra.rendicion;

import com.luadministra.colaboradora.Colaboradora;
import com.luadministra.despacho.Despacho;
import com.luadministra.despacho.DespachoRepository;
import com.luadministra.despacho.EstadoDespacho;
import com.luadministra.despacho.LineaDespacho;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.venta.LineaVentaRequest;
import com.luadministra.venta.VentaRequest;
import com.luadministra.venta.VentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RendicionServiceTest {

    @Mock
    private RendicionRepository rendicionRepository;

    @Mock
    private DespachoRepository despachoRepository;

    @Mock
    private ProductoTerminadoRepository productoTerminadoRepository;

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private RendicionService service;

    private ProductoTerminado producto(Long id, String nombre, double stock) {
        ProductoTerminado p = new ProductoTerminado();
        p.setId(id);
        p.setNombre(nombre);
        p.setStockActual(stock);
        p.setPrecioVenta(100.0);
        return p;
    }

    private Colaboradora colaboradora(Long id, String nombre) {
        Colaboradora c = new Colaboradora();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private LineaDespacho lineaDespacho(Despacho d, ProductoTerminado pt, double cantidad) {
        LineaDespacho ld = new LineaDespacho();
        ld.setDespacho(d);
        ld.setProductoTerminado(pt);
        ld.setCantidad(cantidad);
        ld.setPrecioUnitario(pt.getPrecioVenta());
        return ld;
    }

    @Test
    void listarPorDespacho_devuelveLista() {
        Despacho d = new Despacho();
        d.setId(1L);
        d.setColaboradora(colaboradora(1L, "Ana"));

        Rendicion r = new Rendicion();
        r.setId(1L);
        r.setDespacho(d);
        r.setMontoEntregado(500.0);
        r.setFecha(LocalDate.now());

        when(rendicionRepository.findByDespachoId(1L)).thenReturn(List.of(r));

        List<RendicionResponse> result = service.listarPorDespacho(1L);
        assertEquals(1, result.size());
    }

    @Test
    void listarPorDespacho_sinRendiciones_retornaVacio() {
        when(rendicionRepository.findByDespachoId(1L)).thenReturn(List.of());
        assertTrue(service.listarPorDespacho(1L).isEmpty());
    }

    @Test
    void crear_cuandoDespachoNoExiste_lanzaExcepcion() {
        when(despachoRepository.findById(99L)).thenReturn(Optional.empty());

        RendicionRequest request = new RendicionRequest(99L, List.of(), 0.0, LocalDate.now());
        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(request));
    }

    @Test
    void crear_cuandoDespachoRendidoTotal_lanzaExcepcion() {
        Despacho d = new Despacho();
        d.setId(1L);
        d.setEstado(EstadoDespacho.RENDIDO_TOTAL);
        when(despachoRepository.findById(1L)).thenReturn(Optional.of(d));

        RendicionRequest request = new RendicionRequest(1L, List.of(), 0.0, LocalDate.now());
        assertThrows(SolicitudInvalidaException.class, () -> service.crear(request));
    }

    @Test
    void crear_productoNoEnDespacho_lanzaExcepcion() {
        ProductoTerminado pt = producto(1L, "Jabón", 10.0);
        Despacho d = new Despacho();
        d.setId(1L);
        d.getLineas().add(lineaDespacho(d, pt, 5.0));

        when(despachoRepository.findById(1L)).thenReturn(Optional.of(d));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(99L, 1.0, 0.0)), 100.0, LocalDate.now());
        assertThrows(SolicitudInvalidaException.class, () -> service.crear(request));
    }

    @Test
    void crear_superaDisponible_lanzaExcepcion() {
        ProductoTerminado pt = producto(1L, "Jabón", 10.0);
        Despacho d = new Despacho();
        d.setId(1L);
        d.getLineas().add(lineaDespacho(d, pt, 5.0));

        when(despachoRepository.findById(1L)).thenReturn(Optional.of(d));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 6.0, 0.0)), 100.0, LocalDate.now());
        assertThrows(SolicitudInvalidaException.class, () -> service.crear(request));
    }

    @Test
    void crear_soloDevuelto_restockeaYPersiste() {
        ProductoTerminado pt = producto(1L, "Jabón", 10.0);
        Despacho d = new Despacho();
        d.setId(1L);
        d.setColaboradora(colaboradora(1L, "Ana"));
        d.getLineas().add(lineaDespacho(d, pt, 5.0));

        when(despachoRepository.findById(1L)).thenReturn(Optional.of(d));
        when(rendicionRepository.findByDespachoId(1L)).thenReturn(List.of());
        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(productoTerminadoRepository.getReferenceById(1L)).thenReturn(pt);
        when(rendicionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 0.0, 3.0)), 0.0, LocalDate.now());

        service.crear(request);

        assertEquals(13.0, pt.getStockActual(), 0.001);
        verify(productoTerminadoRepository).save(pt);
        verify(ventaService, never()).crear(any());
        verify(despachoRepository).save(d);
        assertEquals(EstadoDespacho.RENDIDO_PARCIAL, d.getEstado());
    }

    @Test
    void crear_soloVendido_creaVentaYActualizaEstado() {
        ProductoTerminado pt = producto(1L, "Jabón", 10.0);
        Despacho d = new Despacho();
        d.setId(1L);
        d.setColaboradora(colaboradora(1L, "Ana"));
        d.getLineas().add(lineaDespacho(d, pt, 5.0));

        when(despachoRepository.findById(1L)).thenReturn(Optional.of(d));
        when(rendicionRepository.findByDespachoId(1L)).thenReturn(List.of());
        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(productoTerminadoRepository.getReferenceById(1L)).thenReturn(pt);
        when(despachoRepository.save(any())).thenReturn(d);
        when(rendicionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 5.0, 0.0)), 500.0, LocalDate.now());

        service.crear(request);

        ArgumentCaptor<VentaRequest> captor = ArgumentCaptor.forClass(VentaRequest.class);
        verify(ventaService).crear(captor.capture());
        assertEquals(1, captor.getValue().lineas().size());
        assertEquals(5.0, captor.getValue().lineas().get(0).cantidad());

        assertEquals(EstadoDespacho.RENDIDO_TOTAL, d.getEstado());
    }

    @Test
    void crear_consideraRendicionesAnteriores() {
        ProductoTerminado pt = producto(1L, "Jabón", 10.0);
        Despacho d = new Despacho();
        d.setId(1L);
        d.setColaboradora(colaboradora(1L, "Ana"));
        d.getLineas().add(lineaDespacho(d, pt, 10.0));

        Rendicion anterior = new Rendicion();
        anterior.setId(1L);
        LineaRendicion lrAnterior = new LineaRendicion();
        lrAnterior.setProductoTerminado(pt);
        lrAnterior.setCantidadVendida(4.0);
        lrAnterior.setCantidadDevuelta(2.0);
        anterior.getLineas().add(lrAnterior);

        when(despachoRepository.findById(1L)).thenReturn(Optional.of(d));
        when(rendicionRepository.findByDespachoId(1L)).thenReturn(List.of(anterior));
        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(productoTerminadoRepository.getReferenceById(1L)).thenReturn(pt);
        when(rendicionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 0.0, 3.0)), 0.0, LocalDate.now());

        service.crear(request);

        assertEquals(EstadoDespacho.RENDIDO_PARCIAL, d.getEstado());

        verify(despachoRepository).save(d);
        verify(rendicionRepository).save(any());
    }
}
