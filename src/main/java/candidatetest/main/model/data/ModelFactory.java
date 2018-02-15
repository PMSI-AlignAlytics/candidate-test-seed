package candidatetest.main.model.data;

/**
 * This factory is required to provide to the generic repository
 * in order to instantiate new models
 * @param <T> The abstract model class to create
 */
public interface ModelFactory<T extends AbstractModel> {
	
	/**
	 * Create a new instance of the generic model
	 * @return an empty new instance of the generic model
	 */
	T construct();
	
}
