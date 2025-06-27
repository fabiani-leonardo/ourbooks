package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import it.uniroma3.siw.filter.BookFilter;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.repository.BookRepository;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public Book getBook(Long id) {
        Optional<Book> result = this.bookRepository.findById(id);
        return result.orElse(null);
    }

    @Transactional
    public Book saveBook(Book book) {
        return this.bookRepository.save(book);
    }

    /**
     * Restituisce la lista di libri che soddisfano i filtri.
     * Se entrambi i parametri sono null o vuoti, ritorna una lista vuota.
     */
    public List<Book> search(String title, Integer year) {
        // Se non ci sono filtri, non fare alcuna query (pagina vuota)
        if ((title == null || title.isBlank()) && year == null) {
            return List.of();
        }
        // altrimenti applica il filtro
        return bookRepository.findByFilter(
            title != null && !title.isBlank() ? title : null,
            year
        );
    }

    public Book findById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }
    
}