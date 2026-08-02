package com.luadministra.consignacion;

import com.luadministra.consignatario.ConsignatarioRepository;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.rendicion.RendicionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsignacionServiceTest {

    @Mock
    private ConsignacionRepository repository;

    @Mock
    private ConsignatarioRepository consignatarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RendicionRepository rendicionRepository;

    @InjectMocks
    private ConsignacionService service;

    @Test
    void eliminar_sinRendiciones_elimina() {
        Consignacion consignacion = new Consignacion();
        consignacion.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(consignacion));
        when(rendicionRepository.countByConsignacionId(1L)).thenReturn(0L);

        service.eliminar(1L);

        verify(repository).delete(consignacion);
    }

    @Test
    void eliminar_conRendiciones_lanzaExcepcion() {
        Consignacion consignacion = new Consignacion();
        consignacion.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(consignacion));
        when(rendicionRepository.countByConsignacionId(1L)).thenReturn(2L);

        assertThrows(SolicitudInvalidaException.class, () -> service.eliminar(1L));
        verify(repository, never()).delete(any());
    }

    @Test
    void eliminar_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L));
    }
}
