package candidatetest.main.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Concrete implementation of a password encoder
 */
public class PasswordEncoderImpl implements PasswordEncoder {

	/**
	 * Auto-wired BCrypt encoder used to perform the encryption 
	 */
	@Autowired
	private BCryptPasswordEncoder encoder;
	

	/**
	 * Encode plaintext into a one-way cipher
	 * @param plaintext The text to encode
	 * @return A cryptographically secure cipher
	 * @see candidatetest.main.security.PasswordEncoder#encode(java.lang.String)
	 */
	@Override
	public String encode(String plaintext) {
		return encoder.encode(plaintext);
	}

	/**
	 * Test a give plaintext against a given ciphertext and check
	 * whether they represent the same plaintext value
	 * @param plaintext The plaintext to compare
	 * @param cipherText The ciphertext to compare
	 * @return {@code true} if the given ciphertext represents an 
	 * encoded version of the given plaintext
	 * @see candidatetest.main.security.PasswordEncoder#matches(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean matches(String plaintext, String cipherText) {
		return encoder.matches(plaintext, cipherText);
	}

}
