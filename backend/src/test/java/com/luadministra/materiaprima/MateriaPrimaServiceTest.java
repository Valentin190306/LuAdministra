package com.luadministra.materiaprima;

import com.luadministra.categoriamateriaprima.CategoriaMateriaPrimaRepository;
import com.luadministra.compra.CompraRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MateriaPrimaServiceTest {

    @Mock
    private MateriaPrimaRepository repository;

    @Mock
    private CategoriaMateriaPrimaRepository categoriaRepository;

    @Mock
    private CompraRepository compraRepository;

    @InjectMocks
    private MateriaPrimaService service;

    @Test
    void listar_devuelveTodas() {
        when(repository.findAll()).thenReturn(List.of(new MateriaPrima()));
        List<MateriaPrimaResponse> result = service.listar(null, null, null, null);
        assertEquals(1, result.size());
    }

    @Test
    void listar_cuandoVacia_retornaListaVacia() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.listar(null, null, null, null).isEmpty());
    }

    @Test
    void listar_filtraPorNombre() {
        MateriaPrima aceite = new MateriaPrima();
        aceite.setNombre("Aceite de Coco");
        MateriaPrima manteca = new MateriaPrima();
        manteca.setNombre("Manteca de Karite");

        when(repository.findAll()).thenReturn(List.of(aceite, manteca));

        List<MateriaPrimaResponse> result = service.listar("Aceite", null, null, null);
        assertEquals(1, result.size());
        assertEquals("Aceite de Coco", result.get(0).nombre());
    }

    @Test
    void crear_asignaStockCero() {
        MateriaPrimaRequest request = new MateriaPrimaRequest("Manteca de Karite", "gramos", 100.0, null, null);
        MateriaPrima saved = new MateriaPrima();
        saved.setId(1L);
        saved.setNombre("Manteca de Karite");
        saved.setUnidadMedida("gramos");
        saved.setStockActual(0.0);
        saved.setStockMinimo(100.0);

        when(repository.save(any())).thenReturn(saved);

        MateriaPrimaResponse result = service.crear(request);

        assertEquals(0.0, result.stockActual());
        assertEquals("Manteca de Karite", result.nombre());

        ArgumentCaptor<MateriaPrima> captor = ArgumentCaptor.forClass(MateriaPrima.class);
        verify(repository).save(captor.capture());
        assertEquals(0.0, captor.getValue().getStockActual());
    }

    @Test
    void obtener_cuandoExiste_retorna() {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setNombre("Test");
        mp.setUnidadMedida("unidades");
        mp.setStockActual(5.0);

        when(repository.findById(1L)).thenReturn(Optional.of(mp));

        MateriaPrimaResponse result = service.obtener(1L);
        assertEquals("Test", result.nombre());
        assertEquals(5.0, result.stockActual());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void actualizar_modificaDatos() {
        MateriaPrima existente = new MateriaPrima();
        existente.setId(1L);
        existente.setNombre("Original");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        MateriaPrimaResponse result = service.actualizar(1L, new MateriaPrimaRequest("Nuevo", "gramos", null, 50.0, null));

        assertEquals("Nuevo", result.nombre());
        assertEquals("gramos", result.unidadMedida());
        assertEquals(50.0, result.stockMinimo());
    }

    @Test
    void actualizar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.actualizar(99L, new MateriaPrimaRequest("Nada", "gramos", null, null, null)));
    }

    @Test
    void eliminar_borraPorId() {
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
