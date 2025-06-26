package it.uniroma3.siw.controller;

// Importazioni delle classi necessarie
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;

/*con questo controller gestiamo solo la pagina radice*/

@Controller
public class MainController {

	@Autowired	// Inietta automaticamente l'istanza del servizio CredenzialiService
	private CredentialsService credentialsService;

    @Autowired  // Inietta automaticamente l'istanza del servizio UserService (non usato qui ma utile per future estensioni)
	private UserService userService;

	@GetMapping(value = "/")  // Gestisce le richieste GET alla radice del sito ("/")
	public String home(Model model) {  // Il parametro 'model' serve per passare dati alla vista (non usato qui)
		
		// Recupera l'autenticazione corrente dal contesto di sicurezza
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();	//oggetti forniti da Spring Security per conservare nel programma le credenziali in modo sicuro
		
		// Se l'utente non è autenticato (è anonimo), mostra la home pubblica
		if (authentication instanceof AnonymousAuthenticationToken) {	//controlla se l'oggetto authentication è una istanza di un tipo di oggetto creato quando non è stato effettuato l'accesso AnonymousAuthentication
	        return "home.html";
		}
		else {		
			// Recupera i dettagli dell'utente autenticato (principal)
			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();	//come visto anche prima il SecurityHolder contiene tutte le informazioni dell'utente in modo sicuro
			
			// Ottiene le credenziali dal database usando lo username
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			
			// Se l'utente ha ruolo ADMIN, reindirizza alla home dell'area amministratore
			if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
				model.addAttribute("userDetails", userDetails);	//aggiungo ad home.html la possibilità di utilizzare i dati dell'utente autenticato presenti in userDetails
				return "admin/adminHome.html";
			}
			

		}
		
		// Se l’utente è autenticato ma non è un admin, mostra la home standard
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();	//come visto anche prima il SecurityHolder contiene tutte le informazioni dell'utente in modo sicuro
		model.addAttribute("userDetails", userDetails);	//aggiungo ad home.html la possibilità di utilizzare i dati dell'utente autenticato presenti in userDetails
        return "home.html";
    }
}