package candidatetest.main.configuration;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import candidatetest.main.repository.UserRepository;
import candidatetest.main.repository.UserRepositoryImpl;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Configuration class for loading application properties and initialising beans
 */
@Configuration
@ComponentScan(basePackages = "candidatetest.main")
public class CandidateTestConfig {
	
    /**
     * The client to connect to ES 
     * @return An ElasticSearch client used for all data operations
     */
    @Bean
    public Client client() {
        Settings.Builder esSettingsBuilder = Settings.builder()
        	// Add the cluster name from the application properties
            .put("cluster.name", "elasticsearch")
            // Client transport sniff will automatically add or remove
            // data hosts where connecting to a master node
            .put("client.transport.sniff", false)
            // Ignore the cluster name checking on nodes
            .put("client.transport.ignore_cluster_name", true)
            // Enable TCP compression
            .put("transport.tcp.compress", true)
            // Node Sampler Interval 
            .put("client.transport.nodes_sampler_interval", "5s");
        
        // The transport client
        TransportClient client = new PreBuiltTransportClient(esSettingsBuilder.build());
        try {
			client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

        return client;
    }

    /**
     * A Bean to be auto-wired wherever user data is used
     * @param manager The migration manager
     * @return A repository for data operations involving users
     */
    @Autowired
    @Bean
    public UserRepository userRepository() {
    	return new UserRepositoryImpl(client());
    }

}
