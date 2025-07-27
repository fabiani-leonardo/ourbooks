package it.uniroma3.siw.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
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
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.CredentialsService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/authors")
public class AuthorsController {
	
    @Autowired
    private AuthorService authorService;

    @Autowired
    private CredentialsService credentialsService;

    /** Pagina di ricerca autori */
    @GetMapping
    public String showAuthors(Model model) {
        List<Author> authors = authorService.findAll();
        model.addAttribute("authors", authors);
        
        // Gestione utente autenticato/anonimo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
            model.addAttribute("credentials", credentials);
        } else {
            model.addAttribute("credentials", null);
        }
        
        return "/authors/listAuthors";
    }

    /** Dettaglio di un autore */
    @GetMapping("/{id}")
    public String showAuthorDetails(@PathVariable("id") Long id, Model model) {
        Author author = authorService.getAuthor(id);
        if (author != null) {
            model.addAttribute("author", author);
            
            // Gestione utente autenticato/anonimo
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
                model.addAttribute("credentials", credentials);
            } else {
                model.addAttribute("credentials", null);
            }
            
            return "authors/authorDetails";
        } else {
            return "redirect:/authors";
        }
    }

    /** Pagina per aggiungere un nuovo autore */
    @GetMapping("/add")
    public String addAuthorForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/authors";
        }
        
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        if (!credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            return "redirect:/authors";
        }

        model.addAttribute("author", new Author());
        model.addAttribute("credentials", credentials);
        return "/authors/formAddAuthor";
    }
    
    /** Salva nuovo autore */
    @PostMapping("/add")
    public String addAuthor(@Valid @ModelAttribute("author") Author author,
                          BindingResult bindingResult,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return "authors/formAddAuthor";
        }
        
        // Gestione upload immagine
        if (!imageFile.isEmpty()) {
            try {
                String fileName = saveImage(imageFile);
                author.setImage(fileName);
            } catch (IOException e) {
                model.addAttribute("error", "Errore nel caricamento dell'immagine");
                return "authors/formAddAuthor";
            }
        }
        
        this.authorService.saveAuthor(author);
        return "redirect:/authors";
    }
    
    /** Form di modifica autore */
    @GetMapping("/edit/{id}")
    public String editAuthorForm(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/authors";
        }
        
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        if (!credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            return "redirect:/authors";
        }
        
        Author author = authorService.getAuthor(id);
        if (author == null) {
            return "redirect:/authors";
        }
        
        model.addAttribute("author", author);
        model.addAttribute("credentials", credentials);
        return "/authors/formEditAuthor";
    }
    
    /** Salva le modifiche all'autore */
    @PostMapping("/edit/{id}")
    public String updateAuthor(@PathVariable Long id,
                              @Valid @ModelAttribute("author") Author author,
                              BindingResult bindingResult,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              Model model,
                              Principal principal) {
        if (principal == null) {
            return "redirect:/authors";
        }
        
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        if (!credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            return "redirect:/authors";
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("credentials", credentials);
            return "authors/formEditAuthor";
        }
        
        // Recupera l'autore esistente dal database
        Author existingAuthor = authorService.getAuthor(id);
        if (existingAuthor == null) {
            return "redirect:/authors";
        }
        
        // Aggiorna i campi dell'autore esistente
        existingAuthor.setFullName(author.getFullName());
        existingAuthor.setBirthDate(author.getBirthDate());
        existingAuthor.setDeathDate(author.getDeathDate());
        existingAuthor.setNationality(author.getNationality());
        
        // Gestione upload immagine (solo se è stata caricata una nuova immagine)
        if (!imageFile.isEmpty()) {
            try {
                String fileName = saveImage(imageFile);
                existingAuthor.setImage(fileName);
            } catch (IOException e) {
                model.addAttribute("error", "Errore nel caricamento dell'immagine");
                model.addAttribute("credentials", credentials);
                return "authors/formEditAuthor";
            }
        }
        // Se non è stata caricata una nuova immagine, mantieni quella esistente
        
        // Salva le modifiche
        authorService.saveAuthor(existingAuthor);
        
        // Reindirizza ai dettagli dell'autore
        return "redirect:/authors/" + id;
    }
    
    /** Elimina autore */
    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return "redirect:/authors";
        }
        
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        if (!credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            return "redirect:/authors";
        }
        
        Author author = authorService.getAuthor(id);
        if (author != null) {
            // Prima di eliminare, rimuovi l'autore da tutti i libri associati
            if (author.getBooks() != null) {
                for (it.uniroma3.siw.model.Book book : author.getBooks()) {
                    book.getAuthors().remove(author);
                }
            }
            authorService.deleteAuthor(id);
        }
        
        return "redirect:/authors";
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