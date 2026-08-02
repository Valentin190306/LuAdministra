package com.luadministra.lote;

import com.luadministra.dto.PaginatedResponse;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import com.luadministra.receta.RecetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @InjectMocks
    private LoteService service;

    private Lote lote(Long id, LocalDate fecha, Integer diasVigencia) {
        Lote l = new Lote();
        l.setId(id);
        l.setFecha(fecha);
        l.setDiasVigencia(diasVigencia);
        l.setCantidadFabricada(10.0);
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Producto " + id);
        l.setProducto(p);
        return l;
    }

    @Test
    void listar_porVencimiento_asc_conNullsAlFinal() {
        List<Lote> lotes = List.of(
                lote(1L, LocalDate.of(2026, 3, 1), 60),
                lote(2L, LocalDate.of(2026, 1, 1), 30),
                lote(3L, LocalDate.of(2026, 5, 1), null));
        when(loteRepository.findAll()).thenReturn(lotes);

        PaginatedResponse<LoteResponse> result = service.listar(0, 50, "fechaVencimiento", "asc");

        List<LocalDate> fechas = result.content().stream().map(LoteResponse::fechaVencimiento).toList();
        assertEquals(LocalDate.of(2026, 1, 31), fechas.get(0));
        assertEquals(LocalDate.of(2026, 4, 30), fechas.get(1));
        assertNull(fechas.get(2));
        assertEquals(3, result.totalElements());
    }

    @Test
    void listar_porVencimiento_desc_conNullsAlFinal() {
        List<Lote> lotes = List.of(
                lote(1L, LocalDate.of(2026, 3, 1), 60),
                lote(2L, LocalDate.of(2026, 1, 1), 30),
                lote(3L, LocalDate.of(2026, 5, 1), null));
        when(loteRepository.findAll()).thenReturn(lotes);

        PaginatedResponse<LoteResponse> result = service.listar(0, 50, "fechaVencimiento", "desc");

        List<LocalDate> fechas = result.content().stream().map(LoteResponse::fechaVencimiento).toList();
        assertEquals(LocalDate.of(2026, 4, 30), fechas.get(0));
        assertEquals(LocalDate.of(2026, 1, 31), fechas.get(1));
        assertNull(fechas.get(2));
    }

    @Test
    void listar_porVencimiento_respetaPaginacion() {
        List<Lote> lotes = List.of(
                lote(1L, LocalDate.of(2026, 1, 1), 30),
                lote(2L, LocalDate.of(2026, 2, 1), 30),
                lote(3L, LocalDate.of(2026, 3, 1), 30),
                lote(4L, LocalDate.of(2026, 4, 1), 30));
        when(loteRepository.findAll()).thenReturn(lotes);

        PaginatedResponse<LoteResponse> result = service.listar(1, 2, "fechaVencimiento", "asc");

        assertEquals(2, result.content().size());
        assertEquals(LocalDate.of(2026, 3, 31), result.content().get(0).fechaVencimiento());
        assertEquals(LocalDate.of(2026, 5, 1), result.content().get(1).fechaVencimiento());
        assertEquals(4, result.totalElements());
        assertEquals(2, result.totalPages());
    }

    @Test
    void listar_default_ordenaPorColumna() {
        Lote l = lote(1L, LocalDate.of(2026, 1, 1), null);
        PageRequest pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "fecha"));
        when(loteRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(l), pageable, 1));

        PaginatedResponse<LoteResponse> result = service.listar(0, 50, "fecha", "asc");

        assertEquals(1, result.content().size());
        assertEquals(LocalDate.of(2026, 1, 1), result.content().get(0).fecha());
    }
}
