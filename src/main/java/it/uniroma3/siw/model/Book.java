package it.uniroma3.siw.model;

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
	
	@NotNull											//viene richiesto che l'url di un'immagine sia inserito
	private String urlImage;

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

	public String getUrlImage() {
		return urlImage;
	}

	public void setUrlImage(String urlImage) {
		this.urlImage = urlImage;
	}
	
}
