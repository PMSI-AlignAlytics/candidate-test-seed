package candidatetest.main.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import candidatetest.main.security.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import candidatetest.main.model.data.User;
import candidatetest.main.repository.UserRepository;

/**
* User end-point for all API operations relating to the API user
*/
@RequestMapping("/api/development")
@RestController
public class DevelopmentRestController {
		
	/**
	 * The auto-wired user repository for modifying the user models
	 */
	@Autowired
	private UserRepository userRepo;

	/**
	 * The auto-wired password encoder used to encrypt data
	 */
	@Autowired
	private PasswordEncoder encoder;
        
    /**
     * POST /api/development - Builds a set of development data.  This should be cut from production<br><br>
     * 201 - Development data created successfully<br>
     * 401 - This end-point is not enabled in the application configuration<br>
     * 500 - Unknown error occurred
     * @return One of the HTTP responses above
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> create() {
    		    		
		userRepo.createMany(Arrays.asList( 
	    		new User("jbloggs", "Joe Bloggs", encoder.encode("password123"), new String[] { "USER" }),
	    		new User("jdoe", "John Doe", encoder.encode("password123"), new String[] { "USER" }),
				new User("aother", "Anne Other", encoder.encode("password123"), new String[] { "USER" })));
		
		return new ResponseEntity<Void>(new HttpHeaders(), HttpStatus.CREATED);

    }

}
