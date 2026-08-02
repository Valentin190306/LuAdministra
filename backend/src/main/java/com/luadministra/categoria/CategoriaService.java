package com.luadministra.categoria;

import com.luadministra.exception.RecursoNoEncontradoException;
import com.luadministra.materiaprima.MateriaPrimaRepository;
import com.luadministra.producto.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository repository;
    private final MateriaPrimaRepository materiaPrimaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository repository,
                            MateriaPrimaRepository materiaPrimaRepository,
                            ProductoRepository productoRepository) {
        this.repository = repository;
        this.materiaPrimaRepository = materiaPrimaRepository;
        this.productoRepository = productoRepository;
    }

    public List<CategoriaResponse> listar(TipoCategoria tipo) {
        if (tipo != null) {
            return repository.findByTipoOrderByNombreAsc(tipo).stream()
                    .map(CategoriaResponse::fromEntity)
                    .toList();
        }
        return repository.findAll().stream()
                .map(CategoriaResponse::fromEntity)
                .toList();
    }

    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.fromEntity(
                repository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada")));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre());
        categoria.setTipo(request.tipo());
        if (request.categoriaPadreId() != null) {
            categoria.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        }
        return CategoriaResponse.fromEntity(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria existente = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));
        existente.setNombre(request.nombre());
        existente.setTipo(request.tipo());
        if (request.categoriaPadreId() != null) {
            existente.setCategoriaPadre(repository.findById(request.categoriaPadreId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoria padre no encontrada")));
        } else {
            existente.setCategoriaPadre(null);
        }
        return CategoriaResponse.fromEntity(repository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        materiaPrimaRepository.findByCategoriaId(id).forEach(mp -> mp.setCategoria(null));
        productoRepository.findByCategoriaId(id).forEach(p -> p.setCategoria(null));
        repository.findByCategoriaPadreId(id).forEach(hija -> hija.setCategoriaPadre(null));
        repository.deleteById(id);
    }
}
