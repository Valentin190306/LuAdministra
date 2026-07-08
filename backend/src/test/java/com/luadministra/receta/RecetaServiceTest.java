package com.luadministra.receta;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    @Mock
    private RecetaRepository repository;

    @Mock
    private ProductoTerminadoRepository productoTerminadoRepository;

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @InjectMocks
    private RecetaService service;

    @Test
    void guardar_creaRecetaConDetalles() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);

        MateriaPrima mp = new MateriaPrima();
        mp.setId(10L);
        mp.setNombre("Manteca de Karite");

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(materiaPrimaRepository.findById(10L)).thenReturn(Optional.of(mp));
        when(repository.findByProductoTerminadoId(1L)).thenReturn(Optional.empty());

        Receta saved = new Receta();
        saved.setId(1L);
        saved.setProductoTerminado(pt);
        when(repository.save(any())).thenReturn(saved);

        RecetaRequest request = new RecetaRequest(1L, List.of(new RecetaDetalleRequest(10L, 50.0)));
        RecetaResponse result = service.guardar(request);

        assertNotNull(result);
        verify(repository).save(any());
    }

    @Test
    void guardar_cuandoMPNoExiste_lanzaExcepcion() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(repository.findByProductoTerminadoId(1L)).thenReturn(Optional.empty());
        when(materiaPrimaRepository.findById(99L)).thenReturn(Optional.empty());

        RecetaRequest request = new RecetaRequest(1L, List.of(new RecetaDetalleRequest(99L, 50.0)));
        assertThrows(RecursoNoEncontradoException.class, () -> service.guardar(request));
    }

    @Test
    void obtenerPorProducto_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findByProductoTerminadoId(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorProducto(99L));
    }
}
