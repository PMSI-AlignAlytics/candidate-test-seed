package candidatetest.main.repository;

import java.util.List;

import candidatetest.main.model.data.AbstractModel;

/**
 * Implementation of CrudRepository for elastic with extra methods for synchronous calls
 * @param <T> The AbstractModel type for the repository
 */
public interface ElasticRepository<T extends AbstractModel> {

	/**
	 * Save or create the passed entity 
	 * @param entity The entity to save
	 */
	void createOne(T entity);

	/**
	 * Save or create the passed entity 
	 * @param entity The entity to save
	 */
	void createMany(List<T> entities);

	/**
	 * Save or create the passed entity 
	 * @param entity The entity to save
	 */
	T findOne(String id);

}
