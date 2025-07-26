package it.uniroma3.siw.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.CredentialsService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/books")
public class BooksController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CredentialsService credentialsService;

    /** Pagina di ricerca libri */
    @GetMapping
    public String showBooks(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		//User user=credentials.getUser();
		model.addAttribute("credentials", credentials);
		
        return "/books/listBooks";
    }

    /** Dettaglio di un libro */
    @GetMapping("/{id}")
    public String showBookDetails(@PathVariable("id") Long id, Model model) {
        Book book = bookService.findById(id);
        if (book!=null) {
            model.addAttribute("book", book);
            return "books/bookDetails";
        } else {
            return "redirect:/books";
        }
    }

    /** Modifica (placeholder) */
    @GetMapping("/edit/{id}")
    public String booksDetailEdit(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        model.addAttribute("book", book);
        return "bookDetail";
    }

    /** Pagina per aggiungere un nuovo libro */
    @GetMapping("/add")
    public String addBookForm(Model model, Principal principal) {
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        if (!credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            return "redirect:/books";
        }

        model.addAttribute("book", new Book());
        return "/books/formAddBook";
    }
    
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") Book book,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return "books/formAddBook"; // torna al form se ci sono errori di validazione
        }
        this.bookService.save(book);
        return "redirect:/books"; // torna alla lista dei libri
    }
}
