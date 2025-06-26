package it.uniroma3.siw.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
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
}