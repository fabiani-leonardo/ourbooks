// === AuthorService ===
package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.repository.AuthorRepository;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    @Transactional
    public Author getAuthor(Long id) {
        Optional<Author> result = this.authorRepository.findById(id);
        return result.orElse(null);
    }

    @Transactional
    public Author saveAuthor(Author author) {
        return this.authorRepository.save(author);
    }

	public List<Book> findAll() {
		// TODO Auto-generated method stub
		return null;
	}
}