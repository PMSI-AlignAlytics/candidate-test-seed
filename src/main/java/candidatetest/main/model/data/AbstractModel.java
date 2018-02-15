package candidatetest.main.model.data;

import javax.xml.bind.annotation.XmlElement;

/**
 * Base model to enable the Elastic Repository to generate a generic repository
 */
public abstract class AbstractModel {
	
	/** 
	 * The id which will be used when storing the model in the data store
	 */
	protected String id;

	/**
	 * Marks an outlet as deleted, handled automatically in the row filter, any model marked as
	 * deleted will not show up in the queries performed by the ElasticRepository
	 */
	private Boolean deleted;
	
	/**
	 * Force deleted to false by default
	 */
	public AbstractModel() {
		super();
		deleted = false;
	}
	
	/**
	 * Get the id which will be used when storing the model in the data store
	 * @return The id of the document to be used for the data store
	 */
	@XmlElement(nillable=true)
	public String getId() { return id; }
	
	/**
	 * Set the id which will be used when storing the model in the data store
	 * @param id The id of the document to be used for the data store
	 */
	public void setId(String id) { this.id = id; }
	
	/**
	 * Marks an outlet as deleted, handled automatically in the row filter, any model marked as
	 * deleted will not show up in the queries performed by the ElasticRepository
	 * @return a flag Indicating that this outlet should be considered deleted
	 */
	public boolean getDeleted() {
		return deleted;
	}
	
	/**
	 * Marks an outlet as deleted, handled automatically in the row filter, any model marked as
	 * deleted will not show up in the queries performed by the ElasticRepository
	 * @param deleted sets this outlet as deleted
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
		
}
