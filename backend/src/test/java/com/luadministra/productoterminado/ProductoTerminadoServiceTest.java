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
        when(repository.findAll()).thenReturn(List.of(new ProductoTerminado()));
        assertEquals(1, service.listar().size());
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
                new ProductoTerminadoRequest("Shampoo cabello graso", 500.0, null));

        assertEquals(0.0, result.stockActual());

        ArgumentCaptor<ProductoTerminado> captor = ArgumentCaptor.forClass(ProductoTerminado.class);
        verify(repository).save(captor.capture());
        assertEquals(0.0, captor.getValue().getStockActual());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }
}
