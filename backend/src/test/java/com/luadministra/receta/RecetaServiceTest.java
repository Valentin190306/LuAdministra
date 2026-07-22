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

        RecetaRequest request = new RecetaRequest(1L, List.of(new RecetaDetalleRequest(10L, 50.0)), null);
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

        RecetaRequest request = new RecetaRequest(1L, List.of(new RecetaDetalleRequest(99L, 50.0)), null);
        assertThrows(RecursoNoEncontradoException.class, () -> service.guardar(request));
    }

    @Test
    void obtenerPorProducto_cuandoExiste_retorna() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);

        Receta receta = new Receta();
        receta.setId(1L);
        receta.setProductoTerminado(pt);
        receta.setNotas("Nota de prueba");

        when(repository.findByProductoTerminadoId(1L)).thenReturn(Optional.of(receta));

        RecetaResponse result = service.obtenerPorProducto(1L);
        assertNotNull(result);
    }

    @Test
    void obtenerPorProducto_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findByProductoTerminadoId(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorProducto(99L));
    }

    @Test
    void actualizar_modificaNotasYDetalles() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);

        MateriaPrima mp = new MateriaPrima();
        mp.setId(10L);
        mp.setNombre("Manteca de Karite");

        Receta existente = new Receta();
        existente.setId(1L);
        existente.setProductoTerminado(pt);
        existente.setNotas("Vieja nota");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(materiaPrimaRepository.findById(10L)).thenReturn(Optional.of(mp));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        RecetaRequest request = new RecetaRequest(1L, List.of(new RecetaDetalleRequest(10L, 50.0)), "Nueva nota");
        RecetaResponse result = service.actualizar(1L, request);

        verify(repository).save(any());
    }

    @Test
    void actualizar_cuandoProductoNoCoincide_lanzaExcepcion() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);

        Receta existente = new Receta();
        existente.setId(1L);
        existente.setProductoTerminado(pt);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        RecetaRequest request = new RecetaRequest(99L, List.of(), null);
        assertThrows(com.luadministra.exception.SolicitudInvalidaException.class,
                () -> service.actualizar(1L, request));
    }

    @Test
    void eliminar_borraPorId() {
        when(repository.existsById(1L)).thenReturn(true);
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
