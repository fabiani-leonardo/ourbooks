package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthenticationController {
	
	private String formChangeCredentials="/authentication/formChangeCredentials";

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private UserService userService;
	
	// Pagina di registrazione - accesso pubblico (configurato in AuthConfiguration)
	@GetMapping(value = "/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("credentials", new Credentials());
		return "/authentication/formRegisterUser";
	}

	// Pagina di login - accesso pubblico (configurato in AuthConfiguration)
	@GetMapping(value = "/login")
	public String showLoginForm(Model model) {
		return "/authentication/formLogin";
	}

	// Pagina di successo dopo login
	@GetMapping(value = "/success")
	public String defaultAfterLogin(Model model) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		model.addAttribute("userDetails", userDetails);		
		
		// Verifica se è admin e se deve cambiare le credenziali
		if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
			if (credentials.getMustChange()) {
			    return "redirect:/changeCredentials";
			} else {
				return "adminHome";
			}
		}
		return "home";
	}
	
	// Gestione registrazione - accesso pubblico (configurato in AuthConfiguration)
	@PostMapping(value = { "/register" })
	public String registerUser(@Valid @ModelAttribute("user") User user,
							   BindingResult userBindingResult,
							   @Valid @ModelAttribute("credentials") Credentials credentials,
							   BindingResult credenzialiBindingResult,
							   Model model) {

		if (!userBindingResult.hasErrors() && !credenzialiBindingResult.hasErrors()) {
			userService.saveUser(user);
			credentials.setRole(Credentials.DEFAULT_ROLE);
			credentials.setUser(user);
			credentialsService.saveCredentials(credentials);
			model.addAttribute("user", user);
			return "/authentication/registrationSuccessful";
		}
		return "/authentication/formRegisterUser";
	}
	
	// Pagina cambio credenziali - solo utenti autenticati (configurato in AuthConfiguration)
	@GetMapping("/changeCredentials")
	public String showModificaCredenziali(Model model) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());							
	    model.addAttribute("credentials", credentials);
	    return formChangeCredentials;
	}

	// Invio nuove credenziali - solo utenti autenticati (configurato in AuthConfiguration)
	@PostMapping("/modificaCredenziali")
	public String modificaCredenziali(@ModelAttribute("credentials") Credentials newCreds,
	                                  @RequestParam("confermaPassword") String confermaPassword,
	                                  Model model) {

	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    Credentials oldCreds = credentialsService.getCredentials(auth.getName());

	    // Validazione password
	    if (!newCreds.getPassword().equals(confermaPassword)) {
	        model.addAttribute("error", "Le password non coincidono.");
	        return formChangeCredentials;
	    }

	    // Controllo se il nuovo username è già in uso
	    Credentials esistenti = credentialsService.getCredentials(newCreds.getUsername());
	    if (!oldCreds.getUsername().equals(newCreds.getUsername()) && esistenti != null) {
	        model.addAttribute("error", "Username già in uso.");
	        return formChangeCredentials;
	    }

	    // Aggiornamento e salvataggio
	    oldCreds.setUsername(newCreds.getUsername());
	    oldCreds.setPassword(newCreds.getPassword());
	    oldCreds.setMustChange(false);
	    credentialsService.saveCredentials(oldCreds);

	    // Logout forzato per richiedere nuovo login con credenziali aggiornate
	    SecurityContextHolder.clearContext();

	    return "redirect:/login";
	}
}