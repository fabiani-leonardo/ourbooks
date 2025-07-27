package it.uniroma3.siw.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.model.Book;

public interface ReviewRepository extends CrudRepository<Review, Long> {
    
    // Trova la recensione di un utente per un libro specifico
    Optional<Review> findByUserAndBook(User user, Book book);
    
    // Controlla se un utente ha già recensito un libro
    boolean existsByUserAndBook(User user, Book book);
}