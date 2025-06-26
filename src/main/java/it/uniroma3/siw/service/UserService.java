package it.uniroma3.siw.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Aggiungeremo i metodi quando sapremo cosa ci serve
    
    @Transactional
    public User getUser(Long id) {		//chiede a userRepository di trovare un User dato un Id e lo restituisce
        Optional<User> result = this.userRepository.findById(id);
        return result.orElse(null);
    }

    @Transactional
    public User saveUser(User user) {	//chiede a userRepository di salvare l'user fornito
        return this.userRepository.save(user);
    }
}
