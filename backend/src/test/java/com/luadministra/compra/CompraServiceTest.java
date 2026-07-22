package com.luadministra.compra;

import com.luadministra.dto.PaginatedResponse;
import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @InjectMocks
    private CompraService service;

    @Test
    void listar_devuelvePagina() {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setNombre("Aceite");

        Compra compra = new Compra();
        compra.setMateriaPrima(mp);

        Page<Compra> page = new PageImpl<>(List.of(compra));
        when(compraRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PaginatedResponse<CompraResponse> result = service.listar(0, 50, null, null);
        assertEquals(1, result.content().size());
    }

    @Test
    void crear_incrementaStockMP() {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setStockActual(10.0);

        Compra saved = new Compra();
        saved.setId(1L);
        saved.setMateriaPrima(mp);
        saved.setFecha(LocalDate.now());
        saved.setCantidad(5.0);
        saved.setPrecio(100.0);

        when(materiaPrimaRepository.findById(1L)).thenReturn(Optional.of(mp));
        when(compraRepository.save(any())).thenReturn(saved);

        CompraRequest request = new CompraRequest(1L, LocalDate.now(), 5.0, 100.0, null, null, null);
        service.crear(request);

        assertEquals(15.0, mp.getStockActual());
        verify(materiaPrimaRepository).save(mp);
    }

    @Test
    void crear_cuandoMPNoExiste_lanzaExcepcion() {
        when(materiaPrimaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(new CompraRequest(99L, LocalDate.now(), 5.0, 100.0, null, null, null)));
    }

    @Test
    void eliminar_revierteStock() {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(1L);
        mp.setStockActual(20.0);

        Compra compra = new Compra();
        compra.setId(1L);
        compra.setMateriaPrima(mp);
        compra.setCantidad(5.0);

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        service.eliminar(1L);

        assertEquals(15.0, mp.getStockActual());
        verify(materiaPrimaRepository).save(mp);
        verify(compraRepository).delete(compra);
    }

    @Test
    void obtener_cuandoExiste_retorna() {
        MateriaPrima mp = new MateriaPrima();
        mp.setNombre("Aceite de Coco");

        Compra compra = new Compra();
        compra.setId(1L);
        compra.setMateriaPrima(mp);
        compra.setCantidad(5.0);
        compra.setPrecio(100.0);

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        CompraResponse result = service.obtener(1L);
        assertEquals("Aceite de Coco", result.materiaPrimaNombre());
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(compraRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }
}
