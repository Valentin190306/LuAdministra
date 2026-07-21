package com.luadministra.categoriamateriaprima;

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
class CategoriaMateriaPrimaServiceTest {

    @Mock
    private CategoriaMateriaPrimaRepository repository;

    @InjectMocks
    private CategoriaMateriaPrimaService service;

    @Test
    void listar_devuelveTodas() {
        when(repository.findAll()).thenReturn(List.of(new CategoriaMateriaPrima()));
        assertEquals(1, service.listar().size());
    }

    @Test
    void listar_cuandoVacia_retornaListaVacia() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void obtener_cuandoExiste_retorna() {
        CategoriaMateriaPrima c = new CategoriaMateriaPrima();
        c.setId(1L);
        c.setNombre("Aceites");

        when(repository.findById(1L)).thenReturn(Optional.of(c));

        CategoriaMateriaPrimaResponse result = service.obtener(1L);
        assertEquals("Aceites", result.nombre());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void crear_asignaDatosCorrectos() {
        CategoriaMateriaPrima saved = new CategoriaMateriaPrima();
        saved.setId(1L);
        saved.setNombre("Aceites");

        when(repository.save(any())).thenReturn(saved);

        CategoriaMateriaPrimaResponse result = service.crear(new CategoriaMateriaPrimaRequest("Aceites", null));

        assertEquals("Aceites", result.nombre());
        assertNull(result.categoriaPadreId());
    }

    @Test
    void crear_conCategoriaPadre_asignaPadre() {
        CategoriaMateriaPrima padre = new CategoriaMateriaPrima();
        padre.setId(1L);
        padre.setNombre("Grasas");

        CategoriaMateriaPrima saved = new CategoriaMateriaPrima();
        saved.setId(2L);
        saved.setNombre("Aceites");
        saved.setCategoriaPadre(padre);

        when(repository.findById(1L)).thenReturn(Optional.of(padre));
        when(repository.save(any())).thenReturn(saved);

        CategoriaMateriaPrimaResponse result = service.crear(new CategoriaMateriaPrimaRequest("Aceites", 1L));

        assertEquals(1L, result.categoriaPadreId());
        assertEquals("Grasas", result.categoriaPadreNombre());
    }

    @Test
    void actualizar_modificaDatos() {
        CategoriaMateriaPrima existente = new CategoriaMateriaPrima();
        existente.setId(1L);
        existente.setNombre("Original");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoriaMateriaPrimaResponse result = service.actualizar(1L, new CategoriaMateriaPrimaRequest("Modificado", null));

        assertEquals("Modificado", result.nombre());
    }

    @Test
    void actualizar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.actualizar(99L, new CategoriaMateriaPrimaRequest("Nada", null)));
    }

    @Test
    void eliminar_borraPorId() {
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
