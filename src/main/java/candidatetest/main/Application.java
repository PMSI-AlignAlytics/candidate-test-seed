package candidatetest.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Application Entry Point */
@SpringBootApplication
public class Application {

	/** Application Entry Point 
	 * @param args Application start up arguments
	 * */
    public static void main(String[] args) {
    	// Launch the spring application
    	SpringApplication.run(Application.class, args);
    }
        
}
