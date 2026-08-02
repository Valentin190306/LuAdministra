package com.luadministra.producto;

import com.luadministra.categoria.CategoriaRepository;
import com.luadministra.consignacion.ConsignacionRepository;
import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.lote.LoteRepository;
import com.luadministra.receta.RecetaDetalleRepository;
import com.luadministra.venta.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ConsignacionRepository consignacionRepository;

    @Mock
    private RecetaDetalleRepository recetaDetalleRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private ProductoService service;

    @SuppressWarnings("unchecked")
    private <T> Specification<T> anySpec() {
        return any(Specification.class);
    }

    @Test
    void listar_devuelveTodos() {
        when(repository.findAll(anySpec(), any(Sort.class))).thenReturn(List.of(new Producto()));
        List<ProductoResponse> result = service.listar(null, null, null, null);
        assertEquals(1, result.size());
    }

    @Test
    void listarPaginado_devuelvePagina() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Jabon");
        p.setPrecioVenta(50.0);
        p.setStockActual(10.0);
        when(repository.findAll(anySpec(), any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(p), PageRequest.of(0, 50), 1));

        PaginatedResponse<ProductoResponse> result = service.listarPaginado(0, 50, null, null, "nombre", "asc");

        assertEquals(1, result.content().size());
        assertEquals("Jabon", result.content().get(0).nombre());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    void crear_conStockNull_asignaCero() {
        ProductoRequest request = new ProductoRequest("Crema", 100.0, null, null, null);
        Producto saved = new Producto();
        saved.setId(1L);
        saved.setNombre("Crema");
        saved.setPrecioVenta(100.0);
        saved.setStockActual(0.0);

        when(repository.save(any())).thenReturn(saved);

        ProductoResponse result = service.crear(request);
        assertEquals(0.0, result.stockActual());
        assertEquals("Crema", result.nombre());
    }

    @Test
    void crear_conStock_asignaStockInicial() {
        ProductoRequest request = new ProductoRequest("Jabon", 50.0, 25.0, 10.0, null);
        Producto saved = new Producto();
        saved.setId(2L);
        saved.setNombre("Jabon");
        saved.setPrecioVenta(50.0);
        saved.setStockActual(25.0);
        saved.setStockMinimo(10.0);

        when(repository.save(any())).thenReturn(saved);

        ProductoResponse result = service.crear(request);
        assertEquals(25.0, result.stockActual());
        assertEquals(10.0, result.stockMinimo());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void eliminar_borraPorId() {
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void eliminar_cuandoEstaEnReceta_lanzaExcepcion() {
        when(recetaDetalleRepository.countRecetasByProductoId(1L)).thenReturn(1L);

        assertThrows(SolicitudInvalidaException.class, () -> service.eliminar(1L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void eliminar_cuandoTieneLotes_lanzaExcepcion() {
        when(loteRepository.countByProductoId(1L)).thenReturn(2L);

        assertThrows(SolicitudInvalidaException.class, () -> service.eliminar(1L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void eliminar_cuandoTieneVentas_lanzaExcepcion() {
        when(ventaRepository.countLineasByProductoId(1L)).thenReturn(3L);

        assertThrows(SolicitudInvalidaException.class, () -> service.eliminar(1L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void eliminar_cuandoTieneConsignaciones_lanzaExcepcion() {
        when(consignacionRepository.countLineasByProductoId(1L)).thenReturn(4L);

        assertThrows(SolicitudInvalidaException.class, () -> service.eliminar(1L));
        verify(repository, never()).deleteById(any());
    }
}
