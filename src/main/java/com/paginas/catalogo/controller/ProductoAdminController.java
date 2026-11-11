package com.paginas.catalogo.controller;

import com.paginas.catalogo.model.Categoria;
import com.paginas.catalogo.model.Producto;
import com.paginas.catalogo.repository.CategoriaRepository;
import com.paginas.catalogo.repository.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
public class ProductoAdminController {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoAdminController(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    private Producto buscar(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("productos", productos.findAll(Sort.by("nombre")));
        return "admin/productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categorias.findAll());
        return "admin/productos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Producto p = buscar(id);
        model.addAttribute("producto", p);
        model.addAttribute("categoriaId", p.getCategoria().getId());
        model.addAttribute("categorias", categorias.findAll());
        return "admin/productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("producto") Producto form,
                          BindingResult result,
                          @RequestParam(required = false) Long categoriaId,
                          Model model, RedirectAttributes ra) {

        Categoria cat = categoriaId == null ? null : categorias.findById(categoriaId).orElse(null);
        if (cat == null) {
            result.rejectValue("categoria", "required", "Elegí una categoría");
        }
        if (result.hasErrors()) {
            model.addAttribute("categorias", categorias.findAll());
            model.addAttribute("categoriaId", categoriaId);
            return "admin/productos/form";
        }

        Producto p = form.getId() == null ? new Producto() : buscar(form.getId());
        p.setNombre(form.getNombre());
        p.setDescripcion(form.getDescripcion());
        p.setPrecio(form.getPrecio());
        p.setActivo(form.isActivo());
        p.setCategoria(cat);
        productos.save(p);

        ra.addFlashAttribute("mensaje", "Producto guardado");
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/activar")
    public String alternar(@PathVariable Long id, RedirectAttributes ra) {
        Producto p = buscar(id);
        p.setActivo(!p.isActivo());
        productos.save(p);
        ra.addFlashAttribute("mensaje", p.isActivo() ? "Producto activado" : "Producto desactivado");
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        productos.deleteById(id);
        ra.addFlashAttribute("mensaje", "Producto eliminado");
        return "redirect:/admin/productos";
    }
}