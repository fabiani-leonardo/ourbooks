package it.uniroma3.siw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;

/*questa classe è stata introdotta per inizializzare un oggetto User di tipo admin
 * in modo da poterlo usare al primo avvio*/
@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private CredentialsService credentialsService;

    @Override
    public void run(String... args) throws Exception {
        if (!credentialsService.adminExists()) {			//controlliamo se esiste già un admin nel sistema
            User adminUser = new User();					//se non esiste lo creiamo
            adminUser.setName("Admin");						//inizializziamoi suoi dati 
            adminUser.setSurname("Admin");
            adminUser.setEmail("admin@example.com");

            Credentials credentials = new Credentials();	//creiamo un oggetto di tipo credenziali
            credentials.setUsername("admin");				//inseriamo nelle credenziali i dati con cui l'admin accederà per la prima volta prima di cambiarli
            credentials.setPassword("admin");	
            credentials.setRole(Credentials.ADMIN_ROLE);	
            credentials.setUser(adminUser);					//colleghiamo le credenziali allo user creato prima
            credentials.setMustChange(true); 				//Flag che forza il cambio delle credenziali al primo login

            credentialsService.saveCredentials(credentials);//questo serve a salvare le credenziali nel database
        }
    }
}
