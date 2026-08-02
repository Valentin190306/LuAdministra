package com.luadministra.categoria;

import com.luadministra.materiaprima.MateriaPrima;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.producto.Producto;
import com.luadministra.producto.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @Mock
    private MateriaPrimaRepository materiaPrimaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService service;

    @Test
    void listar_filtraPorTipo() {
        when(repository.findByTipoOrderByNombreAsc(TipoCategoria.PRODUCTO)).thenReturn(List.of());
        assertTrue(service.listar(TipoCategoria.PRODUCTO).isEmpty());
        verify(repository).findByTipoOrderByNombreAsc(TipoCategoria.PRODUCTO);
    }

    @Test
    void listar_sinTipo_devuelveTodas() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.listar(null).isEmpty());
        verify(repository).findAll();
    }

    @Test
    void obtener_cuandoNoExiste_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThrows(com.luadministra.exception.RecursoNoEncontradoException.class, () -> service.obtener(99L));
    }

    @Test
    void eliminar_desvinculaMateriasPrimasYProductos() {
        Categoria cat = new Categoria();
        cat.setId(1L);

        MateriaPrima mp = new MateriaPrima();
        mp.setCategoria(cat);

        Producto prod = new Producto();
        prod.setCategoria(cat);

        Categoria hija = new Categoria();
        hija.setCategoriaPadre(cat);

        when(materiaPrimaRepository.findByCategoriaId(1L)).thenReturn(List.of(mp));
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(prod));
        when(repository.findByCategoriaPadreId(1L)).thenReturn(List.of(hija));

        service.eliminar(1L);

        assertNull(mp.getCategoria());
        assertNull(prod.getCategoria());
        assertNull(hija.getCategoriaPadre());
        verify(repository).deleteById(1L);
    }

    @Test
    void eliminar_sinReferencias_soloBorra() {
        when(materiaPrimaRepository.findByCategoriaId(1L)).thenReturn(List.of());
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of());
        when(repository.findByCategoriaPadreId(1L)).thenReturn(List.of());

        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
