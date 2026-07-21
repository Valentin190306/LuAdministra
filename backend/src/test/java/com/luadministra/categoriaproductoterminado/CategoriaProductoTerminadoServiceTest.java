package com.luadministra.categoriaproductoterminado;

import com.luadministra.exception.RecursoNoEncontradoException;
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
class CategoriaProductoTerminadoServiceTest {

    @Mock
    private CategoriaProductoTerminadoRepository repository;

    @InjectMocks
    private CategoriaProductoTerminadoService service;

    @Test
    void listar_devuelveTodas() {
        when(repository.findAll()).thenReturn(List.of(new CategoriaProductoTerminado()));
        assertEquals(1, service.listar().size());
    }

    @Test
    void listar_cuandoVacia_retornaListaVacia() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void obtener_cuandoExiste_retorna() {
        CategoriaProductoTerminado c = new CategoriaProductoTerminado();
        c.setId(1L);
        c.setNombre("Shampoos");

        when(repository.findById(1L)).thenReturn(Optional.of(c));

        CategoriaProductoTerminadoResponse result = service.obtener(1L);
        assertEquals("Shampoos", result.nombre());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void crear_asignaDatosCorrectos() {
        CategoriaProductoTerminado saved = new CategoriaProductoTerminado();
        saved.setId(1L);
        saved.setNombre("Shampoos");

        when(repository.save(any())).thenReturn(saved);

        CategoriaProductoTerminadoResponse result = service.crear(new CategoriaProductoTerminadoRequest("Shampoos", null));

        assertEquals("Shampoos", result.nombre());
        assertNull(result.categoriaPadreId());
    }

    @Test
    void crear_conCategoriaPadre_asignaPadre() {
        CategoriaProductoTerminado padre = new CategoriaProductoTerminado();
        padre.setId(1L);
        padre.setNombre("Rostro");

        CategoriaProductoTerminado saved = new CategoriaProductoTerminado();
        saved.setId(2L);
        saved.setNombre("Jabones");
        saved.setCategoriaPadre(padre);

        when(repository.findById(1L)).thenReturn(Optional.of(padre));
        when(repository.save(any())).thenReturn(saved);

        CategoriaProductoTerminadoResponse result = service.crear(new CategoriaProductoTerminadoRequest("Jabones", 1L));

        assertEquals(1L, result.categoriaPadreId());
        assertEquals("Rostro", result.categoriaPadreNombre());
    }

    @Test
    void actualizar_modificaDatos() {
        CategoriaProductoTerminado existente = new CategoriaProductoTerminado();
        existente.setId(1L);
        existente.setNombre("Original");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoriaProductoTerminadoResponse result = service.actualizar(1L, new CategoriaProductoTerminadoRequest("Modificado", null));

        assertEquals("Modificado", result.nombre());
    }

    @Test
    void actualizar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.actualizar(99L, new CategoriaProductoTerminadoRequest("Nada", null)));
    }

    @Test
    void eliminar_borraPorId() {
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
