package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository; //test con jpaRepository
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Book;

public interface BookRepository extends CrudRepository<Book, Long> {
    // Aggiungeremo metodi personalizzati quando necessario
	
	List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByPublicationYear(int year);
    
    /**
     * Cerca libri in base a titolo (contains) e anno (equals).
     * Se title è null o vuoto, ignora il filtro sul titolo.
     * Se year è null, ignora il filtro sull’anno.
     */
    @Query("SELECT b FROM Book b " +
           "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "  AND (:year  IS NULL OR b.publicationYear = :year)")
    List<Book> findByFilter(
        @Param("title") String title,
        @Param("year")  Integer year
    );

	@Query("SELECT DISTINCT b FROM Book b JOIN b.authors a " +
       "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
       "AND (:year IS NULL OR b.publicationYear = :year) " +
       "AND (:author IS NULL OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :author, '%')))")
	List<Book> findByFilters(@Param("title") String title,
                         @Param("year") Integer year,
                         @Param("author") String author);

	boolean existsByTitleAndPublicationYear(String title, Integer publicationYear);

}