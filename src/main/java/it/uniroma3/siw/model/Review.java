package it.uniroma3.siw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Review {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotBlank(message = "Il titolo non può essere vuoto")
	private String title;

	@NotBlank(message = "Il contenuto non può essere vuoto")
	private String content;

	@Min(value = 1, message = "La valutazione deve essere almeno 1")
	@Max(value = 5, message = "La valutazione non può essere superiore a 5")
	@NotNull(message = "La valutazione è obbligatoria")
	private Integer rating;

	@ManyToOne
	private Book book;

	@ManyToOne
	private User user;

	// Getters e Setters

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getRating() {
	    return rating;
	}

	public void setRating(Integer rating) {
	    this.rating = rating;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
