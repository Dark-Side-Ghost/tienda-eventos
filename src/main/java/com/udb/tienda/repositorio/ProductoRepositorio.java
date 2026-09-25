package com.udb.tienda.repositorio;

import com.udb.tienda.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para Producto.
 * JpaRepository ya nos da los metodos CRUD basicos
 * (save, findById, findAll, deleteById, etc.).
 */
@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
}
