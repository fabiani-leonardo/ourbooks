// === CredentialsService ===
package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;

@Service
public class CredentialsService {
    @Autowired
    private CredentialsRepository credentialsRepository;

    @Transactional
    public Credentials getCredentials(Long id) {
        return this.credentialsRepository.findById(id).orElse(null);
    }

    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        return this.credentialsRepository.save(credentials);
    }

    @Transactional
    public Credentials getCredentials(String username) {
        return this.credentialsRepository.findByUsername(username);
    }
}