package candidatetest.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import candidatetest.main.model.data.User;

/**
* User end-point for all API operations relating to the API user
*/
@RequestMapping("/api/account")
@RestController
public class AccountRestController {
	    /**
     * GET /api/account - Get the authenticated user. It returns one of the following:<br><br>
     * 200 - User returned successfully<br>
     * 500 - Unknown error occurred
     * @return The currently authenticated user
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<User> get() {
        return new ResponseEntity<User>(User.principal(), HttpStatus.OK);
    }

}
