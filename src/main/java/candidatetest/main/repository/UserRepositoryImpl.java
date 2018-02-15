package candidatetest.main.repository;

import java.beans.IntrospectionException;
import java.io.IOException;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

import candidatetest.main.model.data.*;
import candidatetest.main.security.PasswordEncoder;

/**
 * Concrete implementation of the User repository
 */
public class UserRepositoryImpl extends ElasticRepositoryImpl<User> implements UserRepository {

	/**
	 * Auto-wired password encoder used to create and compare one way encryption 
	 */
	@Autowired
	private PasswordEncoder encoder;
	
	/**
	 * This is the default administrator user created when the mapping is initially seeded. They are
	 * required so that a user may create other users. In production this password should be
	 * changed immediately. This is deliberately not overwritten on restart so changed values persist. 
	 */
	private final static User initialAdminCredentials = new User(
			"administrator", 
			"System Administrator",
			"$2a$10$MCCU16p7aq5iHpmhaL09COMC9/ccSneMrFZx60hExsV7jmCK68jcG",
			new String[] { "ADMIN" }); 
	
	/**
	 * Initialise the User repository
	 * @param client The ElasticSearch client for data operations
	 * @param migrationManagerNotifier The migrations manager
	 * @param props Elastic Repository Properties
	 * @param retryTemplate The retry template for calls to the repository
	 * @throws IntrospectionException Thrown if the User POJO reflection failed
     * @throws IndexInitialisationException Index initialisation failed
	 * @throws MappingInitialisationException Mapping initialisation failed
	 * @throws IOException Thrown if the JSON builder fails to create JSON
	 */
	@Autowired
	public UserRepositoryImpl(Client client) {
		super(new User.Factory(), client);
	}
		
	/**
	 * Create an administrator user when the mapping is generated

	 */
	@Override
	protected void seedData() {
		this.createOne(initialAdminCredentials);
	}

}
