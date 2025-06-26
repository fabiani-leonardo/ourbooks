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

/*con questo controller gestiamo tutte le pagine di autenticazione*/

@Controller
public class AuthenticationController {
	
	private String formChangeCredentials="formChangeCredentials";	//uso una variabile per questo form solo perchè richiamerò questa pafina molte volte

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private UserService userService;
	
	//mappiamo la pagina di registrazione
	
	@GetMapping(value = "/register")
	public String showRegisterForm(Model model) {				//questo oggetto mo del deve contenere le variabili che ci servono nella pagina html
		model.addAttribute("user", new User());					//aggiungiamo un nuovo oggetto di tipo User al model in cui metteremo i dati dell'utente che si sta registrando
		model.addAttribute("credentials", new Credentials());	//aggiungiamo un nuovo oggetto di tipo Credentials al model in cui metteremo i dati dell'utente che si sta registrando
		return "formRegisterUser";								//ed ora ritorniamo la pagina con il form di registrazione
	}

	//mappiamo la pagina di login
	
	@GetMapping(value = "/login")
	public String showLoginForm(Model model) {
		return "formLogin";
	}

	/*se l'accesso è andato a buon fine allora veniamo reindirizzati a questa pagina*/
	
	@GetMapping(value = "/success")
	public String defaultAfterLogin(Model model) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //recuperiamo i dati dell'utente che ha eseguito l'accesso al sito tramite il SecurityContextHolder
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		
		/*aggiungiamo al model i dati dell'utente che ha eseguito l'accesso 
		 * in modo che questi siano disponibili per ogni pagina html chiamabile 
		 * da questo metodo cosìcchè l'utente abbia la possibilità di vedere che ha effettuato l'accesso
		 */
		model.addAttribute("userDetails", userDetails);		
		
		/*verifichiamo ora se l'utente che ha acceduto sia un admin, se lo è, verifichiamo anche se
		 nelle sue credenziali la variabile mustChange (che indica se le sue credenziali vanno cambiate)
		 è impostata su true, in tal caso reindirizziamo l'utente a /changeCredentials per cambiare 
		 username e password*/
		if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
			if (credentials.getMustChange()) {
			    return "redirect:/changeCredentials";	//se lo user che ha acceduto è un admin ed ha la flag mustChange impostata su TRUE vuol dire che deve cambiare le proprie credenziali, quindi lo reindirizziamo alla pagina per cambiarle
			}else {
				return "admin/adminHome";				//altrimenti, visto che non deve cambiare le credenziali lo spediamo direttamente alla home degli admin
			}
		}
		return "home";									//se non è admin dopo l'accesso lo mandiamo alla home per users standard
	}
	
	/*metodo che gestisce l'invio al server del form di registrazione*/
	
	@PostMapping(value = { "/register" })
	public String registerUser(@Valid @ModelAttribute("user") User user,
							   BindingResult userBindingResult,
							   @Valid @ModelAttribute("credentials") Credentials credentials,
							   BindingResult credenzialiBindingResult,
							   Model model) {

		if (!userBindingResult.hasErrors() && !credenzialiBindingResult.hasErrors()) {	//se non ci sono errori registriamo il nuovo utente
			userService.saveUser(user);												//salviamo l'utente nel DB
			credentials.setRole(Credentials.DEFAULT_ROLE);
			credentials.setUser(user);												//leghiamo le credenziali all'utente
			credentialsService.saveCredentials(credentials);						//salviamo le credenziali nel DB
			model.addAttribute("user", user);										//aggiugiamo i dati dello user al model in modo che possano essere usati nell'html
			return "registrationSuccessful";
		}
		return "formRegisterUser";														//se i dati inseriti non sono validi l'utente viene rispedito al form
	}
	
	/*metodo che gestisce la richiesta della pagina di cambio credenziali utente*/
	
	@GetMapping("/changeCredentials")
	public String showModificaCredenziali(Model model) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();	//recuperiamo le informazioni dell'utente corrente
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());							
	    model.addAttribute("credentials", credentials);		//aggiungiamo alle variabili della pagina html i dati dell'utente che sta usando il sistema
	    return formChangeCredentials;						//ritorniamo la pagina html con il form per cambiare le credenziali
	}

	/*metodo per inviare al server le nuove credenziali*/
	
	@PostMapping("/modificaCredenziali")
	public String modificaCredenziali(@ModelAttribute("credentials") Credentials newCreds,
	                                  @RequestParam("confermaPassword") String confermaPassword,
	                                  Model model) {

	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    Credentials oldCreds = credentialsService.getCredentials(auth.getName());		//estraiamo dal contesto le credenziali dell'utente attuale

	    // Validazione password
	    //controlliamo che le due password inserite siano uguali tra loro
	    if (!newCreds.getPassword().equals(confermaPassword)) {			//se le due password non coincidono allora:
	        model.addAttribute("error", "Le password non coincidono.");	//viene aggiunto al model un attributo chiamato "error" con il messaggio "Le password non coincidono.".
	        return formChangeCredentials;								//ritorniamo di nuovo il modulo del cambio credenziali ma con il messaggio d'errore
	    }

	    // Controllo se il nuovo username inserito è già in uso
	    Credentials esistenti = credentialsService.getCredentials(newCreds.getUsername());	//prendo dal database qualunque
	    if (!oldCreds.getUsername().equals(newCreds.getUsername()) && esistenti != null) {	//se esiste un utente che già possiede il nuovo username inserito, allora:
	        model.addAttribute("error", "Username già in uso.");							//viene aggiunto al model un attributo chiamato "error" con il messaggio "Username già in uso.".
	        return formChangeCredentials;													//ritorniamo di nuovo il modulo del cambio credenziali ma con il messaggio d'errore
	    }

	    // Aggiornamento e salvataggio (codifica avviene nel service)
	    oldCreds.setUsername(newCreds.getUsername());
	    oldCreds.setPassword(newCreds.getPassword());
	    oldCreds.setMustChange(false);
	    credentialsService.saveCredentials(oldCreds);

	    // Logout forzato per richiedere nuovo login con credenziali aggiornate
	    SecurityContextHolder.clearContext();

	    return "redirect:/login";
	}


	
	
	
	//admin section:
	
	
	
}
