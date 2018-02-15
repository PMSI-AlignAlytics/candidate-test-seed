package candidatetest.main.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import candidatetest.main.model.data.User;
import candidatetest.main.repository.UserRepository;

/**
 * Custom authentication provider used to store user meta data in ElasticSearch
 */
public class ElasticAuthenticationProvider implements AuthenticationProvider {

	/**
	 * Auto-wired password encoder used to create and compare one way encryption 
	 */
	@Autowired
	private PasswordEncoder encoder;
	
	/**
	 * Auto-wired User repository used to read the user and password 
	 */
	@Autowired
	private UserRepository userRepository;
	
	
	/**
	 * Perform an authentication on the given credentials 
	 * @see org.springframework.security.authentication.AuthenticationProvider#authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
    public Authentication authenticate(Authentication authentication) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        User response;
		response = userRepository.findOne(name);
        if (response != null && encoder.matches(password, response.getPassword())) {
        	authorities = response.listAuthorities();
        }
        return new UsernamePasswordAuthenticationToken(response, null, authorities);
    }

	/**
	 * Test whether this authentication provider supports the given token form
	 * @see org.springframework.security.authentication.AuthenticationProvider#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
