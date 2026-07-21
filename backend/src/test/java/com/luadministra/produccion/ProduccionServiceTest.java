package com.luadministra.produccion;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.exception.SolicitudInvalidaException;
import com.luadministra.exception.StockInsuficienteException;
import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.productoterminado.ProductoTerminado;
import com.luadministra.productoterminado.ProductoTerminadoRepository;
import com.luadministra.receta.Receta;
import com.luadministra.receta.RecetaDetalle;
import com.luadministra.receta.RecetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduccionServiceTest {

    @Mock
    private ProduccionRepository produccionRepository;

    @Mock
    private ProductoTerminadoRepository productoTerminadoRepository;

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @InjectMocks
    private ProduccionService service;

    private MateriaPrima crearMP(Long id, String nombre, double stock) {
        MateriaPrima mp = new MateriaPrima();
        mp.setId(id);
        mp.setNombre(nombre);
        mp.setStockActual(stock);
        return mp;
    }

    private Receta crearReceta(ProductoTerminado pt, MateriaPrima mp, double cantidadPorUnidad) {
        RecetaDetalle detalle = new RecetaDetalle();
        detalle.setMateriaPrima(mp);
        detalle.setCantidad(cantidadPorUnidad);

        Receta receta = new Receta();
        receta.setProductoTerminado(pt);
        receta.setDetalles(List.of(detalle));
        detalle.setReceta(receta);
        return receta;
    }

    @Test
    void crear_descruentaStockMP() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setStockActual(0.0);

        MateriaPrima mp = crearMP(10L, "Manteca de Karite", 200.0);
        Receta receta = crearReceta(pt, mp, 30.0);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(recetaRepository.findByProductoTerminadoId(1L)).thenReturn(Optional.of(receta));

        Produccion saved = new Produccion();
        saved.setId(1L);
        saved.setProductoTerminado(pt);
        saved.setCantidadFabricada(3.0);
        when(produccionRepository.save(any())).thenReturn(saved);

        ProduccionRequest request = new ProduccionRequest(1L, LocalDate.now(), 3.0);
        service.crear(request);

        // 30g * 3 unidades = 90g descontados de 200g
        assertEquals(110.0, mp.getStockActual());
        // 3 unidades sumadas al producto
        assertEquals(3.0, pt.getStockActual());
    }

    @Test
    void crear_cuandoStockInsuficiente_lanzaExcepcion() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);

        MateriaPrima mp = crearMP(10L, "Manteca de Karite", 50.0);
        Receta receta = crearReceta(pt, mp, 30.0);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(recetaRepository.findByProductoTerminadoId(1L)).thenReturn(Optional.of(receta));

        ProduccionRequest request = new ProduccionRequest(1L, LocalDate.now(), 3.0);
        // 30g * 3 = 90g necesarios, solo hay 50g
        assertThrows(StockInsuficienteException.class, () -> service.crear(request));
    }

    @Test
    void crear_cuandoNoTieneReceta_lanzaExcepcion() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(recetaRepository.findByProductoTerminadoId(1L)).thenReturn(Optional.empty());

        ProduccionRequest request = new ProduccionRequest(1L, LocalDate.now(), 1.0);
        assertThrows(SolicitudInvalidaException.class, () -> service.crear(request));
    }

    @Test
    void crear_cuandoProductoNoExiste_lanzaExcepcion() {
        when(productoTerminadoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.crear(new ProduccionRequest(99L, LocalDate.now(), 1.0)));
    }

    @Test
    void crear_descruentaMultiplesMP() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setStockActual(0.0);

        MateriaPrima mp1 = crearMP(10L, "Manteca", 100.0);
        MateriaPrima mp2 = crearMP(11L, "Aceite", 200.0);

        RecetaDetalle d1 = new RecetaDetalle();
        d1.setMateriaPrima(mp1);
        d1.setCantidad(20.0);

        RecetaDetalle d2 = new RecetaDetalle();
        d2.setMateriaPrima(mp2);
        d2.setCantidad(10.0);

        Receta receta = new Receta();
        receta.setProductoTerminado(pt);
        receta.setDetalles(List.of(d1, d2));
        d1.setReceta(receta);
        d2.setReceta(receta);

        when(productoTerminadoRepository.findById(1L)).thenReturn(Optional.of(pt));
        when(recetaRepository.findByProductoTerminadoId(1L)).thenReturn(Optional.of(receta));

        Produccion saved = new Produccion();
        saved.setId(1L);
        saved.setProductoTerminado(pt);
        saved.setCantidadFabricada(2.0);
        when(produccionRepository.save(any())).thenReturn(saved);

        ProduccionRequest request = new ProduccionRequest(1L, LocalDate.now(), 2.0);
        service.crear(request);

        assertEquals(60.0, mp1.getStockActual());  // 100 - (20*2)
        assertEquals(180.0, mp2.getStockActual()); // 200 - (10*2)
    }

    @Test
    void eliminar_revierteStockMP() {
        ProductoTerminado pt = new ProductoTerminado();
        pt.setId(1L);
        pt.setStockActual(10.0);

        MateriaPrima mp = crearMP(10L, "Manteca de Karite", 100.0);
        Receta receta = crearReceta(pt, mp, 30.0);

        Produccion produccion = new Produccion();
        produccion.setId(1L);
        produccion.setProductoTerminado(pt);
        produccion.setCantidadFabricada(2.0);

        when(produccionRepository.findById(1L)).thenReturn(Optional.of(produccion));
        when(recetaRepository.findByProductoTerminadoId(1L)).thenReturn(Optional.of(receta));

        service.eliminar(1L);

        assertEquals(160.0, mp.getStockActual()); // 100 + (30*2)
        assertEquals(8.0, pt.getStockActual());   // 10 - 2
        verify(produccionRepository).delete(produccion);
    }
}
