package candidatetest.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import candidatetest.main.model.data.User;
import candidatetest.main.repository.UserRepository;

/**
* User end-point for all API operations relating to the API user
*/
@RequestMapping("/api/user")
@RestController
public class UserRestController {

	/**
	 * The repository to use for data operations
	 */
	@Autowired
	protected UserRepository userRepository;
	
    /**
     * GET /{id} - Single entity retrieval end-point controller method<br><br>
     * 200 - Entity found and returned successfully<br>
     * 404 - No entity were found<br>
     * @param id [Path Variable] The id of the entity to return
     * @return One of the HTTP responses above with a single entity
	 * @throws FindEntityException thrown when an exception occurs during entity read
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    public User get(@PathVariable("id") String id) {
    	return userRepository.findOne(id);
    }

}
