package com.luadministra.colaboradora;

import com.luadministra.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColaboradoraServiceTest {

    @Mock
    private ColaboradoraRepository repository;

    @InjectMocks
    private ColaboradoraService service;

    private Colaboradora colaboradora(Long id, String nombre) {
        Colaboradora c = new Colaboradora();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    @Test
    void listar_devuelveTodas() {
        when(repository.findAll()).thenReturn(List.of(colaboradora(1L, "Ana"), colaboradora(2L, "María")));
        List<ColaboradoraResponse> result = service.listar();
        assertEquals(2, result.size());
    }

    @Test
    void obtener_cuandoExiste_retorna() {
        when(repository.findById(1L)).thenReturn(Optional.of(colaboradora(1L, "Ana")));
        ColaboradoraResponse result = service.obtener(1L);
        assertEquals("Ana", result.nombre());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void crear_guardaYRetorna() {
        Colaboradora saved = colaboradora(1L, "Ana");
        saved.setContacto("11-1234");
        when(repository.save(any())).thenReturn(saved);

        ColaboradoraResponse result = service.crear(new ColaboradoraRequest("Ana", "11-1234"));
        assertEquals("Ana", result.nombre());
        assertEquals("11-1234", result.contacto());
    }

    @Test
    void actualizar_cuandoExiste_modificaYRetorna() {
        Colaboradora existente = colaboradora(1L, "Ana");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ColaboradoraResponse result = service.actualizar(1L, new ColaboradoraRequest("Ana María", "11-5678"));
        assertEquals("Ana María", result.nombre());
    }

    @Test
    void actualizar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.actualizar(99L, new ColaboradoraRequest("X", null)));
    }

    @Test
    void eliminar_eliminaPorId() {
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
