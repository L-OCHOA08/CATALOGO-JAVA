package com.paginas.catalogo.repository;

import com.paginas.catalogo.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Page<Producto> findByActivoTrue(Pageable pageable);
    Page<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId, Pageable pageable);

    long countByCategoriaId(Long categoriaId);
}
