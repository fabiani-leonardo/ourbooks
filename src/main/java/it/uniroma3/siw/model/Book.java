package it.uniroma3.siw.model;

import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Book {
	@Id													//permette di definire questo attributo come chiave
	@GeneratedValue(strategy = GenerationType.AUTO)		//permette di generare automaticamente l'id dell'oggetto
	private Long id;
	
	@NotBlank											//permette di inserire una costrizione sul tipo di valore inserito in questo attributo, in questo caso si richiede che l'attributo non sia vuoto o con caratteri spazio
	private String title;
	
	@Min(-3000)											//permettono di inserire un massimo ed un minimo valore a questo intero
	@Max(2050)
	private Integer publicationYear;
	
	@ElementCollection
	private List<String> imagePaths; // Percorsi relativi a file salvati

	@ManyToMany
	private Set<Author> authors;
	
	@OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
	private List<Review> reviews;
	
	
	
	public List<String> getImagePaths() {
		return imagePaths;
	}

	public void setImagePaths(List<String> imagePaths) {
		this.imagePaths = imagePaths;
	}

	public Set<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<Author> authors) {
		this.authors = authors;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(Integer publicationYear) {
		this.publicationYear = publicationYear;
	}

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	
}