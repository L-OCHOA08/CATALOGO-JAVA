package com.paginas.catalogo.controller;

import com.paginas.catalogo.model.Categoria;
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

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public CategoriaAdminController(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    private Categoria buscar(Long id) {
        return categorias.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // "Pantalones y Shorts" -> "pantalones-y-shorts"
    private String aSlug(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("categorias", categorias.findAll(Sort.by("nombre")));
        return "admin/categorias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "admin/categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", buscar(id));
        return "admin/categorias/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria form,
                          BindingResult result, RedirectAttributes ra) {

        String slug = null;
        if (!result.hasFieldErrors("nombre")) {
            slug = aSlug(form.getNombre());
            if (slug.isEmpty()) {
                result.rejectValue("nombre", "invalido", "El nombre debe tener letras o números");
            } else {
                Optional<Categoria> existente = categorias.findBySlug(slug);
                if (existente.isPresent() && !existente.get().getId().equals(form.getId())) {
                    result.rejectValue("nombre", "duplicado", "Ya existe una categoría con ese nombre");
                }
            }
        }
        if (result.hasErrors()) {
            return "admin/categorias/form";
        }

        Categoria c = form.getId() == null ? new Categoria() : buscar(form.getId());
        c.setNombre(form.getNombre().trim());
        c.setSlug(slug);
        categorias.save(c);

        ra.addFlashAttribute("mensaje", "Categoría guardada");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        Categoria c = buscar(id);
        long cantidad = productos.countByCategoriaId(id);
        if (cantidad > 0) {
            ra.addFlashAttribute("error",
                    "No se puede eliminar \"" + c.getNombre() + "\": tiene " + cantidad
                    + " producto(s). Movelos a otra categoría primero.");
        } else {
            categorias.delete(c);
            ra.addFlashAttribute("mensaje", "Categoría eliminada");
        }
        return "redirect:/admin/categorias";
    }
}