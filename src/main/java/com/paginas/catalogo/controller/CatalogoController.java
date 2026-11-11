package com.paginas.catalogo.controller;

import com.paginas.catalogo.model.Categoria;
import com.paginas.catalogo.model.Producto;
import com.paginas.catalogo.repository.CategoriaRepository;
import com.paginas.catalogo.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CatalogoController {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public CatalogoController(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @GetMapping("/")
    public String inicio(@RequestParam(defaultValue = "0") int pagina, Model model) {
        Page<Producto> page = productos.findByActivoTrue(
                PageRequest.of(pagina, 12, Sort.by("nombre")));
        model.addAttribute("productos", page);
        model.addAttribute("categorias", categorias.findAll());
        return "catalogo";
    }

    @GetMapping("/categoria/{slug}")
    public String porCategoria(@PathVariable String slug,
                               @RequestParam(defaultValue = "0") int pagina, Model model) {
        Categoria cat = categorias.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Producto> page = productos.findByCategoriaIdAndActivoTrue(
                cat.getId(), PageRequest.of(pagina, 12, Sort.by("nombre")));
        model.addAttribute("productos", page);
        model.addAttribute("categorias", categorias.findAll());
        model.addAttribute("categoriaActual", cat);
        return "catalogo";
    }
}