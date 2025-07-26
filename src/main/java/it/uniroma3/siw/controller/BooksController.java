package it.uniroma3.siw.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        
        // Gestione utente autenticato/anonimo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
            model.addAttribute("credentials", credentials);
        } else {
            model.addAttribute("credentials", null);
        }
        
        return "/books/listBooks";
    }

    /** Dettaglio di un libro */
    @GetMapping("/{id}")
    public String showBookDetails(@PathVariable("id") Long id, Model model) {
        Book book = bookService.findById(id);
        if (book != null) {
            model.addAttribute("book", book);
            
            // Gestione utente autenticato/anonimo
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
                model.addAttribute("credentials", credentials);
            } else {
                model.addAttribute("credentials", null);
            }
            
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
        if (principal == null) {
            return "redirect:/books";
        }
        
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        if (!credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            return "redirect:/books";
        }

        model.addAttribute("book", new Book());
        model.addAttribute("credentials", credentials);
        return "/books/formAddBook";
    }
    
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") Book book,
                          BindingResult bindingResult,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return "books/formAddBook"; // torna al form se ci sono errori di validazione
        }
        
        // Gestione upload immagine
        if (!imageFile.isEmpty()) {
            try {
                String fileName = saveImage(imageFile);
                book.setImagePath(fileName);
            } catch (IOException e) {
                model.addAttribute("error", "Errore nel caricamento dell'immagine");
                return "books/formAddBook";
            }
        }
        
        this.bookService.save(book);
        return "redirect:/books"; // torna alla lista dei libri
    }
    
    private String saveImage(MultipartFile file) throws IOException {
        // Genera un nome file unico
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + extension;
        
        // Percorso della cartella images
        Path uploadPath = Paths.get("src/main/resources/static/images");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Salva il file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return fileName;
    }
}