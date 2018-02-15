package candidatetest.main.security;

/**
 * Generic password encoder interface to wrap the implementation specific BCrypt one
 */
public interface PasswordEncoder {
	
	/**
	 * Encode plaintext into a one-way cipher
	 * @param plaintext The text to encode
	 * @return A cryptographically secure cipher
	 */
	String encode(String plaintext);
	
	/**
	 * Test a give plaintext against a given ciphertext and check
	 * whether they represent the same plaintext value
	 * @param plaintext The plaintext to compare
	 * @param cipherText The ciphertext to compare
	 * @return {@code true} if the given ciphertext represents an 
	 * encoded version of the given plaintext 
	 */
	Boolean matches(String plaintext, String cipherText);
	
}
