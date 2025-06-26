package it.uniroma3.siw.repository;

import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.model.Author;

public interface AuthorRepository extends CrudRepository<Author, Long> {
}