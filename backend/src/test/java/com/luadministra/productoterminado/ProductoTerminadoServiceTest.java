package com.luadministra.productoterminado;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTerminadoServiceTest {

    @Mock
    private ProductoTerminadoRepository repository;

    @InjectMocks
    private ProductoTerminadoService service;

    @Test
    void listar_devuelveTodos() {
        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(new ProductoTerminado()));
        assertEquals(1, service.listar(null, null, null, null).size());
    }

    @Test
    void listar_cuandoVacia_retornaListaVacia() {
        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of());
        assertTrue(service.listar(null, null, null, null).isEmpty());
    }

    @Test
    void listar_filtraPorNombre() {
        ProductoTerminado jabon = new ProductoTerminado();
        jabon.setNombre("Jabón de Lavanda");
        ProductoTerminado shampoo = new ProductoTerminado();
        shampoo.setNombre("Shampoo");

        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(jabon, shampoo));

        List<ProductoTerminadoResponse> result = service.listar("Jabón", null, null, null);
        assertEquals(1, result.size());
        assertEquals("Jabón de Lavanda", result.get(0).nombre());
    }

    @Test
    void crear_asignaStockCero() {
        ProductoTerminado saved = new ProductoTerminado();
        saved.setId(1L);
        saved.setNombre("Shampoo cabello graso");
        saved.setPrecioVenta(500.0);
        saved.setStockActual(0.0);

        when(repository.save(any())).thenReturn(saved);

        ProductoTerminadoResponse result = service.crear(
                new ProductoTerminadoRequest("Shampoo cabello graso", 500.0, null, null, null));

        assertEquals(0.0, result.stockActual());

        ArgumentCaptor<ProductoTerminado> captor = ArgumentCaptor.forClass(ProductoTerminado.class);
        verify(repository).save(captor.capture());
        assertEquals(0.0, captor.getValue().getStockActual());
    }

    @Test
    void obtener_cuandoExiste_retorna() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setNombre("Jabón de Lavanda");
        pt.setPrecioVenta(2000.0);

        when(repository.findById(1L)).thenReturn(Optional.of(pt));

        ProductoTerminadoResponse result = service.obtener(1L);
        assertEquals("Jabón de Lavanda", result.nombre());
        assertEquals(2000.0, result.precioVenta());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void actualizar_modificaDatos() {
        ProductoTerminado existente = new ProductoTerminado();
        existente.setId(1L);
        existente.setNombre("Original");
        existente.setPrecioVenta(1000.0);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProductoTerminadoResponse result = service.actualizar(1L,
                new ProductoTerminadoRequest("Modificado", 2500.0, null, null, null));

        assertEquals("Modificado", result.nombre());
        assertEquals(2500.0, result.precioVenta());
    }

    @Test
    void actualizar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.actualizar(99L, new ProductoTerminadoRequest("Nada", 0.0, null, null, null)));
    }

    @Test
    void eliminar_borraPorId() {
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
