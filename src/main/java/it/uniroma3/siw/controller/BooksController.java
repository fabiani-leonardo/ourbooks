package it.uniroma3.siw.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/books")
public class BooksController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CredentialsService credentialsService;
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private ReviewService reviewService;

    /** Pagina di ricerca libri */
    @GetMapping
    public String showBooks(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        addCredentialsToModel(model);
        return "/books/listBooks";
    }

    /** Dettaglio di un libro */
    @GetMapping("/{id}")
    public String showBookDetails(@PathVariable("id") Long id, Model model) {
        Book book = bookService.findById(id);
        if (book != null) {
            model.addAttribute("book", book);
            addCredentialsToModel(model);
            
            // Recupera la recensione dell'utente corrente per questo libro (se esiste)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
                Review userReview = reviewService.getUserReviewForBook(credentials.getUser(), book);
                model.addAttribute("userReview", userReview);
            } else {
                model.addAttribute("userReview", null);
            }
            
            return "books/bookDetails";
        } else {
            return "redirect:/books";
        }
    }

    /** Form di modifica libro - Solo ADMIN (controllato da Spring Security) */
    @GetMapping("/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        if (book == null) {
            return "redirect:/books";
        }
        
        model.addAttribute("book", book);
        addCredentialsToModel(model);
        return "/books/formEditBook";
    }
    
    /** Salva le modifiche al libro - Solo ADMIN (controllato da Spring Security) */
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Long id,
                            @Valid @ModelAttribute("book") Book book,
                            BindingResult bindingResult,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            Model model) {
        
        if (bindingResult.hasErrors()) {
            addCredentialsToModel(model);
            return "books/formEditBook";
        }
        
        // Recupera il libro esistente dal database
        Book existingBook = bookService.findById(id);
        if (existingBook == null) {
            return "redirect:/books";
        }
        
        // Aggiorna i campi del libro esistente
        existingBook.setTitle(book.getTitle());
        existingBook.setPublicationYear(book.getPublicationYear());
        
        // Gestione upload immagine (solo se è stata caricata una nuova immagine)
        if (!imageFile.isEmpty()) {
            try {
                String fileName = saveImage(imageFile);
                existingBook.setImagePath(fileName);
            } catch (IOException e) {
                model.addAttribute("error", "Errore nel caricamento dell'immagine");
                addCredentialsToModel(model);
                return "books/formEditBook";
            }
        }
        
        // Salva le modifiche
        bookService.save(existingBook);
        
        // Reindirizza ai dettagli del libro
        return "redirect:/books/" + id;
    }

    /** Pagina per aggiungere un nuovo libro - Solo ADMIN (controllato da Spring Security) */
    @GetMapping("/add")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        addCredentialsToModel(model);
        return "/books/formAddBook";
    }
    
    /** Salva nuovo libro - Solo ADMIN (controllato da Spring Security) */
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") Book book,
                          BindingResult bindingResult,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return "books/formAddBook";
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
        
        // Salva il libro PRIMA
        Book savedBook = this.bookService.save(book);
        
        // Poi reindirizza alla pagina di modifica autori
        return "redirect:/books/" + savedBook.getId() + "/authors-edit";
    }
    
    /** Pagina di selezione autori - Solo ADMIN (controllato da Spring Security) */
    @GetMapping("/{id}/authors-edit")
    public String editBookAuthors(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        if (book == null) {
            return "redirect:/books";
        }
        
        // Usa il metodo del service per ottenere gli autori disponibili
        List<Author> availableAuthors = authorService.getAvailableAuthorsForBook(book);
        
        model.addAttribute("book", book);
        model.addAttribute("availableAuthors", availableAuthors);
        addCredentialsToModel(model);
        
        return "books/editBookAuthors";
    }

    /** Aggiungi un autore al libro - Solo ADMIN (controllato da Spring Security) */
    @GetMapping("/{bookId}/authors/add/{authorId}")
    public String addAuthorToBook(@PathVariable Long bookId, 
                                  @PathVariable Long authorId) {
        Book book = bookService.findById(bookId);
        Author author = authorService.getAuthor(authorId);
        
        if (book != null && author != null) {
            if (book.getAuthors() == null) {
                book.setAuthors(new HashSet<>());
            }
            book.getAuthors().add(author);
            bookService.save(book);
        }
        
        return "redirect:/books/" + bookId + "/authors-edit";
    }

    /** Rimuovi un autore dal libro - Solo ADMIN (controllato da Spring Security) */
    @GetMapping("/{bookId}/authors/remove/{authorId}")
    public String removeAuthorFromBook(@PathVariable Long bookId, 
                                       @PathVariable Long authorId) {
        Book book = bookService.findById(bookId);
        Author author = authorService.getAuthor(authorId);
        
        if (book != null && author != null && book.getAuthors() != null) {
            book.getAuthors().remove(author);
            bookService.save(book);
        }
        
        return "redirect:/books/" + bookId + "/authors-edit";
    }
    
    /** Elimina libro - Solo ADMIN (controllato da Spring Security) */
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        Book book = bookService.findById(id);
        if (book != null) {
            bookService.deleteById(id);
        }
        
        return "redirect:/books";
    }
    
    /** Pagina di ricerca libri */
    @GetMapping("/find")
    public String showFindBooksForm(Model model) {
        addCredentialsToModel(model);
        return "/books/findBooks";
    }

    /** Esegui ricerca libri */
    @PostMapping("/find")
    public String searchBooks(@RequestParam("title") String title, Model model) {
        addCredentialsToModel(model);
        
        List<Book> searchResults = null;
        
        if (title != null && !title.trim().isEmpty()) {
            // Effettua la ricerca per titolo
            searchResults = bookService.findByTitle(title.trim());
            model.addAttribute("searchTerm", title.trim());
            model.addAttribute("books", searchResults);
        }
        
        return "/books/findBooks";
    }
    
    /** Metodo helper per aggiungere le credenziali al model */
    private void addCredentialsToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
            model.addAttribute("credentials", credentials);
        } else {
            model.addAttribute("credentials", null);
        }
    }
    
    /** Metodo helper per salvare le immagini */
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