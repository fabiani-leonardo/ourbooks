package it.uniroma3.siw.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.service.BookService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reviews")
public class ReviewsController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService;

    @Autowired
    private CredentialsService credentialsService;

    /** Form per aggiungere una nuova recensione ad un libro */
    @GetMapping("/add/{bookId}")
    public String addReviewForm(@PathVariable Long bookId, Model model, Principal principal) {
        // Recupera il libro
        Book book = bookService.findById(bookId);
        if (book == null) {
            return "redirect:/books";
        }

        // Recupera le credenziali dell'utente (Spring Security garantisce che principal non è null)
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        
        // Controlla se l'utente ha già scritto una recensione per questo libro
        if (reviewService.hasUserReviewedBook(credentials.getUser(), book)) {
            // Se ha già recensito, reindirizza alla pagina del libro
            return "redirect:/books/" + bookId;
        }

        // Crea una nuova recensione vuota
        Review review = new Review();
        review.setBook(book);
        review.setUser(credentials.getUser());

        model.addAttribute("review", review);
        model.addAttribute("book", book);
        model.addAttribute("credentials", credentials);
        
        return "reviews/formAddReview";
    }

    /** Salva la nuova recensione */
    @PostMapping("/add/{bookId}")
    public String addReview(@PathVariable Long bookId,
                           @Valid @ModelAttribute("review") Review review,
                           BindingResult bindingResult,
                           Model model,
                           Principal principal) {
        // Recupera il libro
        Book book = bookService.findById(bookId);
        if (book == null) {
            return "redirect:/books";
        }

        // Recupera le credenziali dell'utente (Spring Security garantisce che principal non è null)
        Credentials credentials = credentialsService.getCredentials(principal.getName());

        // Validazione personalizzata per la lunghezza dei campi
        if (review.getTitle() != null && review.getTitle().length() > 255) {
            bindingResult.rejectValue("title", "error.review", "Il titolo non può superare i 255 caratteri");
        }
        
        if (review.getContent() != null && review.getContent().length() > 255) {
            bindingResult.rejectValue("content", "error.review", "Il contenuto non può superare i 255 caratteri");
        }

        // Controlla se ci sono errori di validazione
        if (bindingResult.hasErrors()) {
            // Riassegna book e user alla recensione per evitare che si perdano
            review.setBook(book);
            review.setUser(credentials.getUser());
            
            model.addAttribute("review", review);
            model.addAttribute("book", book);
            model.addAttribute("credentials", credentials);
            return "reviews/formAddReview";
        }

        // Controlla di nuovo se l'utente ha già recensito questo libro
        if (reviewService.hasUserReviewedBook(credentials.getUser(), book)) {
            return "redirect:/books/" + bookId;
        }

        // Imposta i dati della recensione
        review.setBook(book);
        review.setUser(credentials.getUser());

        // Salva la recensione
        reviewService.saveReview(review);

        return "redirect:/books/" + bookId;
    }

    /** Form per modificare una recensione esistente */
    @GetMapping("/edit/{reviewId}")
    public String editReviewForm(@PathVariable Long reviewId, Model model, Principal principal) {
        // Recupera la recensione
        Review review = reviewService.getReview(reviewId);
        if (review == null) {
            return "redirect:/books";
        }

        // Recupera le credenziali dell'utente (Spring Security garantisce che principal non è null)
        Credentials credentials = credentialsService.getCredentials(principal.getName());

        // Controlla se l'utente è il proprietario della recensione
        if (!review.getUser().getId().equals(credentials.getUser().getId())) {
            return "redirect:/books/" + review.getBook().getId();
        }

        model.addAttribute("review", review);
        model.addAttribute("book", review.getBook());
        model.addAttribute("credentials", credentials);

        return "reviews/formEditReview";
    }

    /** Salva le modifiche alla recensione */
    @PostMapping("/edit/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                              @Valid @ModelAttribute("review") Review updatedReview,
                              BindingResult bindingResult,
                              Model model,
                              Principal principal) {
        // Recupera la recensione esistente
        Review existingReview = reviewService.getReview(reviewId);
        if (existingReview == null) {
            return "redirect:/books";
        }

        // Recupera le credenziali dell'utente (Spring Security garantisce che principal non è null)
        Credentials credentials = credentialsService.getCredentials(principal.getName());

        // Controlla se l'utente è il proprietario della recensione
        if (!existingReview.getUser().getId().equals(credentials.getUser().getId())) {
            return "redirect:/books/" + existingReview.getBook().getId();
        }

        // Validazione personalizzata per la lunghezza dei campi
        if (updatedReview.getTitle() != null && updatedReview.getTitle().length() > 255) {
            bindingResult.rejectValue("title", "error.review", "Il titolo non può superare i 255 caratteri");
        }
        
        if (updatedReview.getContent() != null && updatedReview.getContent().length() > 255) {
            bindingResult.rejectValue("content", "error.review", "Il contenuto non può superare i 255 caratteri");
        }

        // Controlla se ci sono errori di validazione
        if (bindingResult.hasErrors()) {
            // Importante: riassegna l'ID alla recensione nel form per evitare che diventi null
            updatedReview.setId(existingReview.getId());
            updatedReview.setBook(existingReview.getBook());
            updatedReview.setUser(existingReview.getUser());
            
            model.addAttribute("review", updatedReview);
            model.addAttribute("book", existingReview.getBook());
            model.addAttribute("credentials", credentials);
            return "reviews/formEditReview";
        }

        // Aggiorna i campi della recensione esistente
        existingReview.setTitle(updatedReview.getTitle());
        existingReview.setContent(updatedReview.getContent());
        existingReview.setRating(updatedReview.getRating());

        // Salva le modifiche
        reviewService.saveReview(existingReview);

        return "redirect:/books/" + existingReview.getBook().getId();
    }

    /** Cancella una recensione */
    @GetMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId, Principal principal) {
        // Recupera la recensione
        Review review = reviewService.getReview(reviewId);
        if (review == null) {
            return "redirect:/books";
        }

        // Recupera le credenziali dell'utente (Spring Security garantisce che principal non è null)
        Credentials credentials = credentialsService.getCredentials(principal.getName());
        Long bookId = review.getBook().getId();

        // Controlla se l'utente può cancellare la recensione
        // (è il proprietario della recensione O è un admin)
        boolean canDelete = review.getUser().getId().equals(credentials.getUser().getId()) ||
                           credentials.getRole().equals(Credentials.ADMIN_ROLE);

        if (canDelete) {
            reviewService.deleteReview(reviewId);
        }

        return "redirect:/books/" + bookId;
    }
}