// === AuthorService ===
package it.uniroma3.siw.service;

import java.util.ArrayList;
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

	public List<Author> findAll() {
		return (List<Author>) authorRepository.findAll();
	}
	
	@Transactional
	public List<Author> getAvailableAuthorsForBook(Book book) {
	    List<Author> allAuthors = this.findAll();
	    List<Author> availableAuthors = new ArrayList<>();
	    
	    for (Author author : allAuthors) {
	        // Se il libro non ha autori oppure questo autore non è già nel libro
	        if (book.getAuthors() == null || !book.getAuthors().contains(author)) {
	            availableAuthors.add(author);
	        }
	    }
	    
	    return availableAuthors;
	}
	
	@Transactional
	public void deleteAuthor(Long id) {
	    this.authorRepository.deleteById(id);
	}
}