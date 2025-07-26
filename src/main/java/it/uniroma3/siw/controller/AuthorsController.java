package it.uniroma3.siw.controller;

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
    
    @PostMapping("/add")
    public String addAuthor(@Valid @ModelAttribute("author") Author author,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return "authors/formAddAuthor"; // torna al form se ci sono errori di validazione
        }
        this.authorService.saveAuthor(author);
        return "redirect:/authors"; // torna alla lista degli autori
    }
}