package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.service.BookService;

@Controller
@RequestMapping("/books")
public class BooksController {

    @Autowired
    private BookService bookService;

    /** Pagina di ricerca libri (inizialmente vuota) */
    @GetMapping
    public String books(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer year,
            Model model) {

        List<Book> results = bookService.search(title, year);

        model.addAttribute("books", results);
        model.addAttribute("title", title);
        model.addAttribute("year",  year);
        return "books";
    }

    /** Dettaglio di un singolo libro */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        model.addAttribute("book", book);
        return "bookDetail";  // pagina dedicata per il libro
    }
}
