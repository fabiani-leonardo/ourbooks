package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.service.AuthorService;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.CredentialsService;

@Controller
@RequestMapping("/books")
public class AuthorsController {
	
	   @Autowired
	    private AuthorService authorService;

	    @Autowired
	    private CredentialsService credentialsService;
	
	
	/** Pagina di ricerca libri */
    @GetMapping
    public String showBooks(Model model) {
        List<Book> books = authorService.findAll();
        model.addAttribute("books", books);
        
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		//User user=credentials.getUser();
		model.addAttribute("credentials", credentials);
		
        return "/books/listBooks";
    }
}
