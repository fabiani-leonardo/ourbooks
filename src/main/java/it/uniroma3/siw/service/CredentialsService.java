package it.uniroma3.siw.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;
import jakarta.transaction.Transactional;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;
    
    @Autowired
    protected PasswordEncoder passwordEncoder;

    // Aggiungeremo i metodi quando sapremo cosa ci serve
    
    
    /*chiede a credenzialiRepository di trovare un oggetto credenziali dato un id e lo restituisce*/
    @Transactional
    public Credentials getCredentials(Long id) {
        Optional<Credentials> result = this.credentialsRepository.findById(id);
        return result.orElse(null);
    }
    
    /*chiede a credenzialiRepository di trovare un oggetto credenziali dato uno username e lo restituisce*/
    @Transactional
    public Credentials getCredentials(String username) {
        Optional<Credentials> result = this.credentialsRepository.findByUsername(username);
        return result.orElse(null);
    }

    /*chiee a credenzialiRepository di salvare l'oggetto Credenziali fornito ma solo dopo aver cryptato la password*/
    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        if(!credentials.getRole().equals(Credentials.ADMIN_ROLE)) {		
        	credentials.setRole(Credentials.DEFAULT_ROLE);
        }
        credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        return this.credentialsRepository.save(credentials);
    }

    public boolean adminExists() {	//questo metodo serve a verificare l'esistenza di almeno un admin nel db
        return credentialsRepository.existsByRole(Credentials.ADMIN_ROLE);
    }

}