package it.uniroma3.siw.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static it.uniroma3.siw.model.Credentials.ADMIN_ROLE;
import static it.uniroma3.siw.model.Credentials.DEFAULT_ROLE;

import javax.sql.DataSource;

@Configuration								
@EnableWebSecurity							
public class AuthConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .authoritiesByUsernameQuery("SELECT username, role from credentials WHERE username=?")
                .usersByUsernameQuery("SELECT username, password, 1 as enabled FROM credentials WHERE username=?");
    }
    
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf().and().cors().disable()
                .authorizeHttpRequests()
                
                // === ACCESSO PUBBLICO (tutti possono accedere) ===
                .requestMatchers(HttpMethod.GET, "/", "/books", "/books/{id}", "/books/find", "/authors", "/authors/{id}", 
                                "/register", "/login", "/css/**", "/images/**", "favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/register", "/login", "/books/find").permitAll()
                
                // === SOLO ADMIN ===
                .requestMatchers(HttpMethod.GET, "/admin/**", 
                                "/books/add", "/books/edit/**", "/books/delete/**", "/books/{bookId}/authors-edit", 
                                "/books/{bookId}/authors/add/**", "/books/{bookId}/authors/remove/**",
                                "/authors/add", "/authors/edit/**", "/authors/delete/**").hasAuthority(ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST, "/admin/**", "/books/add", "/books/edit/**", 
                                "/authors/add", "/authors/edit/**").hasAuthority(ADMIN_ROLE)
                
                // === UTENTI AUTENTICATI (admin + utenti normali) ===
                .requestMatchers(HttpMethod.GET, "/reviews/add/**", "/reviews/edit/**", "/reviews/delete/**", 
                                "/changeCredentials").authenticated()
                .requestMatchers(HttpMethod.POST, "/reviews/add/**", "/reviews/edit/**", 
                                "/modificaCredenziali").authenticated()
                
                // === TUTTO IL RESTO RICHIEDE AUTENTICAZIONE ===
                .anyRequest().authenticated()
                
                // === LOGIN ===
                .and().formLogin()
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/success", true)
                .failureUrl("/login?error=true")
                
                // === LOGOUT ===
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .clearAuthentication(true).permitAll();
                
        return httpSecurity.build();
    }
}