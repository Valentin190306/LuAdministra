package com.luadministra.consignatario;

import com.luadministra.consignacion.ConsignacionRepository;
import com.luadministra.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsignatarioServiceTest {

    @Mock
    private ConsignatarioRepository repository;

    @Mock
    private ConsignacionRepository consignacionRepository;

    @InjectMocks
    private ConsignatarioService service;

    @Test
    void eliminar_sinConsignaciones_elimina() {
        when(consignacionRepository.countByConsignatarioId(1L)).thenReturn(0L);

        service.eliminar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void eliminar_conConsignaciones_lanzaExcepcion() {
        when(consignacionRepository.countByConsignatarioId(1L)).thenReturn(3L);

        assertThrows(SolicitudInvalidaException.class, () -> service.eliminar(1L));
        verify(repository, never()).deleteById(anyLong());
    }
}
