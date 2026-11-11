package com.paginas.catalogo.controller;

import com.paginas.catalogo.model.Categoria;
import com.paginas.catalogo.model.Producto;
import com.paginas.catalogo.repository.CategoriaRepository;
import com.paginas.catalogo.repository.ProductoRepository;
import com.paginas.catalogo.service.ImagenStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/productos")
public class ProductoAdminController {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final ImagenStorageService storage;

    public ProductoAdminController(ProductoRepository productos,
                                   CategoriaRepository categorias,
                                   ImagenStorageService storage) {
        this.productos = productos;
        this.categorias = categorias;
        this.storage = storage;
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
                          @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                          @RequestParam(defaultValue = "false") boolean quitarImagen,
                          Model model, RedirectAttributes ra) throws IOException {

        Categoria cat = categoriaId == null ? null : categorias.findById(categoriaId).orElse(null);
        if (cat == null) {
            result.rejectValue("categoria", "required", "Elegí una categoría");
        }

        String nuevaImagen = null;
        String errorImagen = null;
        if (!result.hasErrors()) {
            try {
                nuevaImagen = storage.guardar(archivo);
            } catch (IllegalArgumentException e) {
                errorImagen = e.getMessage();
            }
        }

        if (result.hasErrors() || errorImagen != null) {
            model.addAttribute("categorias", categorias.findAll());
            model.addAttribute("categoriaId", categoriaId);
            model.addAttribute("errorImagen", errorImagen);
            return "admin/productos/form";
        }

        Producto p = form.getId() == null ? new Producto() : buscar(form.getId());
        String anterior = p.getImagen();

        p.setNombre(form.getNombre());
        p.setDescripcion(form.getDescripcion());
        p.setPrecio(form.getPrecio());
        p.setActivo(form.isActivo());
        p.setCategoria(cat);

        boolean reemplazar = nuevaImagen != null;
        if (reemplazar) {
            p.setImagen(nuevaImagen);
        } else if (quitarImagen) {
            p.setImagen(null);
        }
        productos.save(p);

        if (reemplazar || quitarImagen) {
            storage.borrar(anterior);
        }

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
        Producto p = buscar(id);
        String imagen = p.getImagen();
        productos.delete(p);
        storage.borrar(imagen);
        ra.addFlashAttribute("mensaje", "Producto eliminado");
        return "redirect:/admin/productos";
    }
}