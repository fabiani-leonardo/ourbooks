package it.uniroma3.siw.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.model.Book;
import it.uniroma3.siw.repository.ReviewRepository;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public Review getReview(Long id) {
        Optional<Review> result = this.reviewRepository.findById(id);
        return result.orElse(null);
    }

    @Transactional
    public Review saveReview(Review review) {
        return this.reviewRepository.save(review);
    }
    
    @Transactional
    public void deleteReview(Long id) {
        this.reviewRepository.deleteById(id);
    }
    
    @Transactional
    public Review getUserReviewForBook(User user, Book book) {
        Optional<Review> result = this.reviewRepository.findByUserAndBook(user, book);
        return result.orElse(null);
    }
    
    @Transactional
    public boolean hasUserReviewedBook(User user, Book book) {
        return this.reviewRepository.existsByUserAndBook(user, book);
    }
}