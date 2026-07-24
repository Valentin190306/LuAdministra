package com.luadministra.despacho;

import com.luadministra.colaboradora.Colaboradora;
import com.luadministra.colaboradora.ColaboradoraRepository;
import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.rendicion.LineaRendicion;
import com.luadministra.rendicion.Rendicion;
import com.luadministra.rendicion.RendicionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DespachoServiceTest {

    @Mock
    private DespachoRepository repository;

    @Mock
    private ColaboradoraRepository colaboradoraRepository;

    @Mock
    private ProductoTerminadoRepository productoTerminadoRepository;

    @Mock
    private RendicionRepository rendicionRepository;

    @InjectMocks
    private DespachoService service;

    private Colaboradora colaboradora(Long id, String nombre) {
        Colaboradora c = new Colaboradora();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    private ProductoTerminado producto(Long id, String nombre) {
        ProductoTerminado p = new ProductoTerminado();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecioVenta(100.0);
        return p;
    }

    private LineaDespacho linea(Despacho d, ProductoTerminado pt, double cantidad) {
        LineaDespacho ld = new LineaDespacho();
        ld.setDespacho(d);
        ld.setProductoTerminado(pt);
        ld.setCantidad(cantidad);
        return ld;
    }

    @Test
    void listar_devuelvePagina() {
        Despacho despacho = new Despacho();
        despacho.setId(1L);
        despacho.setColaboradora(colaboradora(1L, "Ana"));
        Page<Despacho> page = new PageImpl<>(List.of(despacho));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        PaginatedResponse<DespachoResponse> result = service.listar(0, 50, null, null, null, null);
        assertEquals(1, result.content().size());
    }

    @Test
    void listar_filtraPorColaboradora() {
        when(repository.findByColaboradoraId(1L, PageRequest.of(0, 50, Sort.by("fecha"))))
                .thenReturn(Page.empty());
        PaginatedResponse<DespachoResponse> result = service.listar(0, 50, null, null, 1L, null);
        assertTrue(result.content().isEmpty());
    }

    @Test
    void listar_filtraPorEstado() {
        when(repository.findByEstado(EstadoDespacho.PENDIENTE, PageRequest.of(0, 50, Sort.by("fecha"))))
                .thenReturn(Page.empty());
        PaginatedResponse<DespachoResponse> result = service.listar(0, 50, null, null, null, "PENDIENTE");
        assertTrue(result.content().isEmpty());
    }

    @Test
    void listar_filtraPorColaboradoraYEstado() {
        when(repository.findByColaboradoraIdAndEstado(1L, EstadoDespacho.PENDIENTE,
                PageRequest.of(0, 50, Sort.by("fecha")))).thenReturn(Page.empty());
        PaginatedResponse<DespachoResponse> result = service.listar(0, 50, null, null, 1L, "PENDIENTE");
        assertTrue(result.content().isEmpty());
    }

    @Test
    void obtener_cuandoExiste_retorna() {
        Colaboradora colab = colaboradora(1L, "Ana");
        Despacho despacho = new Despacho();
        despacho.setId(1L);
        despacho.setColaboradora(colab);
        when(repository.findById(1L)).thenReturn(Optional.of(despacho));

        DespachoResponse result = service.obtener(1L);
        assertEquals("Ana", result.colaboradoraNombre());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void crear_guardaDespachoConLineas() {
        Colaboradora colab = colaboradora(1L, "Ana");
        ProductoTerminado pt = producto(1L, "Jabón");
        when(colaboradoraRepository.findById(1L)).thenReturn(Optional.of(colab));
        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DespachoRequest request = new DespachoRequest(1L, LocalDate.now(),
                List.of(new LineaDespachoRequest(1L, 5.0)));

        DespachoResponse result = service.crear(request);
        assertEquals(1, result.productos().size());
        assertEquals("Jabón", result.productos().get(0).productoTerminadoNombre());
    }

    @Test
    void crear_colaboradoraNoExiste_lanzaExcepcion() {
        when(colaboradoraRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(new DespachoRequest(99L, LocalDate.now(),
                        List.of(new LineaDespachoRequest(1L, 5.0)))));
    }

    @Test
    void eliminar_cuandoExiste_elimina() {
        Despacho despacho = new Despacho();
        despacho.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(despacho));

        service.eliminar(1L);
        verify(repository).delete(despacho);
    }

    @Test
    void eliminar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
    }

    @Test
    void calcularStockDespachado_sumaLineasActivas() {
        ProductoTerminado pt = producto(1L, "Jabón");
        Despacho d1 = new Despacho();
        d1.getLineas().add(linea(d1, pt, 10.0));
        Despacho d2 = new Despacho();
        d2.getLineas().add(linea(d2, pt, 5.0));

        when(repository.findLineasByProductoTerminadoIdAndEstadoNot(1L, EstadoDespacho.RENDIDO_TOTAL))
                .thenReturn(List.of(d1.getLineas().get(0), d2.getLineas().get(0)));

        Double result = service.calcularStockDespachado(1L);
        assertEquals(15.0, result);
    }

    @Test
    void calcularStockDespachado_sinLineas_retornaCero() {
        when(repository.findLineasByProductoTerminadoIdAndEstadoNot(1L, EstadoDespacho.RENDIDO_TOTAL))
                .thenReturn(List.of());
        assertEquals(0.0, service.calcularStockDespachado(1L));
    }

    @Test
    void stockConsignado_retornaAgregado() {
        ProductoTerminado pt1 = producto(1L, "Jabón");
        ProductoTerminado pt2 = producto(2L, "Shampoo");

        Despacho d1 = new Despacho();
        d1.setId(1L);
        d1.setColaboradora(colaboradora(1L, "Ana"));
        d1.getLineas().add(linea(d1, pt1, 10.0));
        d1.getLineas().add(linea(d1, pt2, 5.0));

        Rendicion rendicion = new Rendicion();
        rendicion.setId(1L);
        rendicion.setDespacho(d1);

        LineaRendicion lr = new LineaRendicion();
        lr.setProductoTerminado(pt1);
        lr.setCantidadVendida(4.0);
        lr.setCantidadDevuelta(1.0);
        rendicion.getLineas().add(lr);

        when(colaboradoraRepository.existsById(1L)).thenReturn(true);
        when(repository.findActivosByColaboradoraId(1L)).thenReturn(List.of(d1));
        when(rendicionRepository.findByDespachoId(1L)).thenReturn(List.of(rendicion));

        List<StockConsignadoResponse> result = service.stockConsignado(1L);
        assertEquals(2, result.size());

        StockConsignadoResponse r1 = result.stream().filter(r -> r.productoTerminadoId().equals(1L)).findFirst().orElseThrow();
        assertEquals(10.0, r1.cantidadDespachada());
        assertEquals(5.0, r1.cantidadRendida());
        assertEquals(5.0, r1.cantidadPendiente());

        StockConsignadoResponse r2 = result.stream().filter(r -> r.productoTerminadoId().equals(2L)).findFirst().orElseThrow();
        assertEquals(5.0, r2.cantidadDespachada());
        assertEquals(0.0, r2.cantidadRendida());
        assertEquals(5.0, r2.cantidadPendiente());
    }

    @Test
    void stockConsignado_sinDespachos_retornaVacio() {
        when(colaboradoraRepository.existsById(1L)).thenReturn(true);
        when(repository.findActivosByColaboradoraId(1L)).thenReturn(List.of());
        assertTrue(service.stockConsignado(1L).isEmpty());
    }

    @Test
    void stockConsignado_colaboradoraNoExiste_lanzaExcepcion() {
        when(colaboradoraRepository.existsById(99L)).thenReturn(false);
        assertThrows(RecursoNoEncontradoException.class, () -> service.stockConsignado(99L));
    }

    @Test
    void stockConsignado_excluyeDespachosRendidosTotal() {
        ProductoTerminado pt = producto(1L, "Jabón");
        Despacho d = new Despacho();
        d.setId(1L);
        d.setEstado(EstadoDespacho.RENDIDO_TOTAL);
        d.getLineas().add(linea(d, pt, 10.0));

        when(colaboradoraRepository.existsById(1L)).thenReturn(true);
        when(repository.findActivosByColaboradoraId(1L)).thenReturn(List.of());
        assertTrue(service.stockConsignado(1L).isEmpty());
    }
}
