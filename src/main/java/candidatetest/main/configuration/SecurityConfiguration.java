package candidatetest.main.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import candidatetest.main.security.ElasticAuthenticationProvider;
import candidatetest.main.security.PasswordEncoder;
import candidatetest.main.security.PasswordEncoderImpl;

/**
 * A Custom security configuration which limits user access to areas of the API and
 * sets the {@link AuthenticationProvider} as the authentication provider to use
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
    /**
     * A Bean to be auto-wired wherever the custom authentication
     * provider is required
     * @return An authentication provider using Elastic search storage
     */    
    @Bean
    public AuthenticationProvider authProvider() {
    	return new ElasticAuthenticationProvider();
    }

    /**
     * A Bean to be auto-wired wherever BCrypt specific data encryption is required
     * @return The BCrypt password encoder for encrypting and decrypting
     * using the latest security standard
     */
    @Bean
    public BCryptPasswordEncoder baseEncoder() {
    	return new BCryptPasswordEncoder();
    }
    
    /** 
     * A Bean to be auto-wired wherever general data encryption is required. 
     * This wraps the BCrypt encoder, but is given a generic interface in case
     * we need to upgrade this encryption at a later date (though of course this
     * would require some handling of already encrypted data)
     * @return A generic password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
    	return new PasswordEncoderImpl();
    }

	/**
	 * The authentication realm for blocking all site authentication
	 */
	private static String REALM = "scaramanga";

	/**
	 * Return the authentication realm
	 * @return A string name for the realm
	 */
	public static String getRealm() {
		return REALM;
	}

	/**
	 * Wire the authentication provider to use 
	 */
	@Autowired private AuthenticationProvider authProvider;

	/**
	 * Set the authentication provider from the auto-wired property
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder)
	 */
	@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	auth.authenticationProvider(authProvider);
    }
	
	/**
	 * Configure the HTTP security and restrict access to areas of the API
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Rules for ant-matchers need to be defined from most specific to most vague
		// once the ant string matches the check stops and returns that result even if it fails
		http.csrf().disable()
		  	.authorizeRequests()
  	    		.antMatchers("/api/status").permitAll()
		  	    .antMatchers("/api/**").hasRole("ADMIN")
			.and()
				// Direct API calls need to be able to use basic auth
				.httpBasic() 
		  	.and()
		  		.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
 	}

    /**
     * Configure the ignore path matchers
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.WebSecurity)
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
    }
}
