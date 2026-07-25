package com.luadministra.rendicion;

import com.luadministra.consignatario.Consignatario;
import com.luadministra.consignacion.Consignacion;
import com.luadministra.consignacion.ConsignacionRepository;
import com.luadministra.consignacion.EstadoConsignacion;
import com.luadministra.consignacion.LineaConsignacion;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
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
    private ConsignacionRepository consignacionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private RendicionService service;

    private Producto producto(Long id, String nombre, double stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setStockActual(stock);
        p.setPrecioVenta(100.0);
        return p;
    }

    private Consignatario consignatario(Long id, String nombre) {
        Consignatario c = new Consignatario();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private LineaConsignacion lineaConsignacion(Long id, Consignacion d, Producto pt, double cantidad) {
        LineaConsignacion ld = new LineaConsignacion();
        ld.setId(id);
        ld.setConsignacion(d);
        ld.setProducto(pt);
        ld.setCantidad(cantidad);
        ld.setPrecioUnitario(pt.getPrecioVenta());
        return ld;
    }

    @Test
    void listarPorConsignacion_devuelveLista() {
        Consignacion d = new Consignacion();
        d.setId(1L);
        d.setConsignatario(consignatario(1L, "Ana"));

        Rendicion r = new Rendicion();
        r.setId(1L);
        r.setConsignacion(d);
        r.setMontoEntregado(500.0);
        r.setFecha(LocalDate.now());

        when(rendicionRepository.findByConsignacionId(1L)).thenReturn(List.of(r));

        List<RendicionResponse> result = service.listarPorConsignacion(1L);
        assertEquals(1, result.size());
    }

    @Test
    void listarPorConsignacion_sinRendiciones_retornaVacio() {
        when(rendicionRepository.findByConsignacionId(1L)).thenReturn(List.of());
        assertTrue(service.listarPorConsignacion(1L).isEmpty());
    }

    @Test
    void crear_cuandoConsignacionNoExiste_lanzaExcepcion() {
        when(consignacionRepository.findById(99L)).thenReturn(Optional.empty());

        RendicionRequest request = new RendicionRequest(99L, List.of(), 0.0, LocalDate.now());
        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(request));
    }

    @Test
    void crear_cuandoConsignacionRendidoTotal_lanzaExcepcion() {
        Consignacion d = new Consignacion();
        d.setId(1L);
        d.setEstado(EstadoConsignacion.RENDIDO_TOTAL);
        when(consignacionRepository.findById(1L)).thenReturn(Optional.of(d));

        RendicionRequest request = new RendicionRequest(1L, List.of(), 0.0, LocalDate.now());
        assertThrows(SolicitudInvalidaException.class, () -> service.crear(request));
    }

    @Test
    void crear_productoNoEnConsignacion_lanzaExcepcion() {
        Producto pt = producto(1L, "Jabón", 10.0);
        Consignacion d = new Consignacion();
        d.setId(1L);
        d.getLineas().add(lineaConsignacion(1L, d, pt, 5.0));

        when(consignacionRepository.findById(1L)).thenReturn(Optional.of(d));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(99L, 1.0, 0.0)), 100.0, LocalDate.now());
        assertThrows(SolicitudInvalidaException.class, () -> service.crear(request));
    }

    @Test
    void crear_superaDisponible_lanzaExcepcion() {
        Producto pt = producto(1L, "Jabón", 10.0);
        Consignacion d = new Consignacion();
        d.setId(1L);
        d.getLineas().add(lineaConsignacion(1L, d, pt, 5.0));

        when(consignacionRepository.findById(1L)).thenReturn(Optional.of(d));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 6.0, 0.0)), 100.0, LocalDate.now());
        assertThrows(SolicitudInvalidaException.class, () -> service.crear(request));
    }

    @Test
    void crear_soloDevuelto_restockeaYPersiste() {
        Producto pt = producto(1L, "Jabón", 10.0);
        Consignacion d = new Consignacion();
        d.setId(1L);
        d.setConsignatario(consignatario(1L, "Ana"));
        d.getLineas().add(lineaConsignacion(1L, d, pt, 5.0));

        when(consignacionRepository.findById(1L)).thenReturn(Optional.of(d));
        when(rendicionRepository.findByConsignacionId(1L)).thenReturn(List.of());
        when(rendicionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 0.0, 3.0)), 0.0, LocalDate.now());

        service.crear(request);

        assertEquals(13.0, pt.getStockActual(), 0.001);
        verify(productoRepository).save(pt);
        verify(ventaService, never()).crear(any());
        verify(consignacionRepository).save(d);
        assertEquals(EstadoConsignacion.RENDIDO_PARCIAL, d.getEstado());
    }

    @Test
    void crear_soloVendido_creaVentaYActualizaEstado() {
        Producto pt = producto(1L, "Jabón", 10.0);
        Consignacion d = new Consignacion();
        d.setId(1L);
        d.setConsignatario(consignatario(1L, "Ana"));
        d.getLineas().add(lineaConsignacion(1L, d, pt, 5.0));

        when(consignacionRepository.findById(1L)).thenReturn(Optional.of(d));
        when(rendicionRepository.findByConsignacionId(1L)).thenReturn(List.of());
        when(consignacionRepository.save(any())).thenReturn(d);
        when(rendicionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 5.0, 0.0)), 500.0, LocalDate.now());

        service.crear(request);

        ArgumentCaptor<VentaRequest> captor = ArgumentCaptor.forClass(VentaRequest.class);
        verify(ventaService).crear(captor.capture());
        assertEquals(1, captor.getValue().lineas().size());
        assertEquals(5.0, captor.getValue().lineas().get(0).cantidad());

        assertEquals(EstadoConsignacion.RENDIDO_TOTAL, d.getEstado());
    }

    @Test
    void crear_consideraRendicionesAnteriores() {
        Producto pt = producto(1L, "Jabón", 10.0);
        Consignacion d = new Consignacion();
        d.setId(1L);
        d.setConsignatario(consignatario(1L, "Ana"));
        d.getLineas().add(lineaConsignacion(1L, d, pt, 10.0));

        LineaConsignacion lc = lineaConsignacion(1L, d, pt, 10.0);
        d.getLineas().clear();
        d.getLineas().add(lc);

        Rendicion anterior = new Rendicion();
        anterior.setId(1L);
        LineaRendicion lrAnterior = new LineaRendicion();
        lrAnterior.setLineaConsignacion(lc);
        lrAnterior.setCantidadVendida(4.0);
        lrAnterior.setCantidadDevuelta(2.0);
        anterior.getLineas().add(lrAnterior);

        when(consignacionRepository.findById(1L)).thenReturn(Optional.of(d));
        when(rendicionRepository.findByConsignacionId(1L)).thenReturn(List.of(anterior));
        when(rendicionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RendicionRequest request = new RendicionRequest(1L,
                List.of(new LineaRendicionRequest(1L, 0.0, 3.0)), 0.0, LocalDate.now());

        service.crear(request);

        assertEquals(EstadoConsignacion.RENDIDO_PARCIAL, d.getEstado());

        verify(consignacionRepository).save(d);
        verify(rendicionRepository).save(any());
    }
}
