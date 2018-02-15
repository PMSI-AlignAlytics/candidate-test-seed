package candidatetest.main.model.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The user model representing a user of this API
 */
@XmlRootElement(name = "user")
public class User extends AbstractModel {

	/**
	 * The user readable user name
	 */
	private String fullName;
	/**
	 * An encrypted password - the password will be stored as it is in this model, therefore it should not
	 * be plain-text
	 */
	private String password;
	/**
	 * The roles for this user.  These roles are prefixed with "ROLE_" and granted as authorities 
	 */
	private String[] roles;
	
	/**
	 * The factory for creating new empty instances of this model
	 */
	public static class Factory implements ModelFactory<User> {
		
		/** Construct a new empty instance of the User model
		 * @see candidatetest.main.model.data.ModelFactory#construct()
		 */
		@Override
		public User construct() {
			return new User();
		}	
		
	}
	
	/**
	 * Default empty constructor for the model 
	 */
	public User() {
		super();
	}
	
	/**
	 * Construct a populated user object
	 * @param userName The login name of the user
	 * @param fullName The user readable name of the user
	 * @param password The encrypted password for access to this API
	 * @param roles The user roles which this user has.  These are used to control access to areas of the API
	 * @param acquirers The acquirers which the user belongs to.  These are used to control row level access to repositories
	 * @param suppliers The suppliers which the user belongs to.  These are used to control row level access to repositories
	 */
	public User(String userName, String fullName, String password, String[] roles) {
		this();
		this.setId(userName);
		this.setFullName(fullName);
		this.setRoles(roles);
		this.setPassword(password);
		
		// User must always have one of the two roles: USER or ADMIN
		// If none is present, then we must add USER
		Boolean hasMandatoryRole = false;
		if (roles != null) {
			for (String role : roles) {
				hasMandatoryRole = hasMandatoryRole || role == "USER" || role == "ADMIN";
			}
		}
		if (!hasMandatoryRole) {
			if (this.roles != null) {
				String[] newRoles = Arrays.copyOf(this.roles, this.roles.length + 1);
				newRoles[this.roles.length] = "USER";
				this.roles = newRoles;
			} else {
				this.roles = new String[] { "USER" };
			}
		}
	}

	/**
	 * Get the user readable name of the user
	 * @return The user readable name of the user
	 */
	@XmlElement
	public String getFullName() { 
		return fullName; 
	}
	/**
	 * Set the user readable name of the user
	 * @param fullName The readable name of the user
	 */
	public void setFullName(String fullName) { 
		this.fullName = fullName; 
	}

	/**
	 * Get the encrypted password
	 * @return The encrypted user password
	 */
	@XmlElement
	public String getPassword() { 
		return password; 
	}
	/**
	 * Set the encrypted password
	 * @param password The encrypted user password
	 */
	public void setPassword(String password) { 
		this.password = password; 
	}
	/**
	 * Get the roles for controlling user access to API areas
	 * @return The roles for controlling user access to API areas
	 */
	@XmlElement
	public String[] getRoles() { 
		return roles; 
	}
	/** 
	 * Set the roles for controlling user access to API areas
	 * @param roles The roles for controlling user access to API areas
	 */
	public void setRoles(String[] roles) { 
		this.roles = roles; 
	}
	
	/**
	 * Get a list of Authority objects based on this users roles.  This should be
	 * used to control access to areas of the API
	 * @return A list of {@link org.springframework.security.core.GrantedAuthority} objects based on this users roles
	 */
	public List<GrantedAuthority> listAuthorities() {
		ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
    	for (String role : this.getRoles()) {
    		auths.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));        		
    	}	
    	return auths;
	}
	
	/**
	 * Checks whether a user has a role of the passed name
	 * @param role The role to check.  This is not an authority therefore the string should for example be {@code "USER"} as opposed to {@code "ROLE_USER"} for an authority  
	 * @return {@code True} if the user has the passed role
	 */
	public Boolean hasRole(String role) {
		Boolean flag = false;
		if (roles != null) {
			for (String r : roles) {
				flag = flag || r.equalsIgnoreCase(role);
			}
		}
		return flag;
	}
	
	/**
	 * Checks whether a user has a role of the passed name
	 * @param roles The roles to check.  This is not an authority therefore the string should for example be {@code "USER"} as opposed to {@code "ROLE_USER"} for an authority  
	 * @return {@code True} if the user has the passed role
	 */
	public Boolean hasAnyRole(String... roles) {
		Boolean flag = false;
		if (roles != null) {
			for (String r : roles) {
				flag = flag || Arrays.asList(this.getRoles()).contains(r);
			}
		}
		return flag;
	}
		
	/**
	 * Static method for retrieving the currently authenticated user
	 * @return A {@link candidatetest.main.model.data.User} object for the authenticated user
	 */
	public static User principal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    return auth == null ? null : (User) auth.getPrincipal();
	}
			
	/**
	 * Convert the user to a string for JWT
	 * @param input The user to serialize
	 * @return A string representation of the user which can be deserialized
	 * @throws JsonProcessingException 
	 */
	public static String serialize(User input) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(input);
	}
	
	/**
	 * Convert the string to a user
	 * @param input The string to deserialize
	 * @return A string representation of the user
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static User deserialize(String input) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(input, User.class);
	}
	
}
