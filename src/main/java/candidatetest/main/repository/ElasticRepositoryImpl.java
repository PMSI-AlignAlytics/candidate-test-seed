package candidatetest.main.repository;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.DocWriteRequest.OpType;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.GenericTypeResolver;

import com.carrotsearch.hppc.ObjectLookupContainer;
import com.carrotsearch.hppc.cursors.ObjectCursor;

import candidatetest.main.model.data.AbstractModel;
import candidatetest.main.model.data.ModelFactory;
import candidatetest.main.repository.DataField.FieldType;

/**
 * Generic repository for storing and accessing a Java model in ElasticSearch
 * @param <T> The type of model being accessed.  This type should inherit from {@link candidatetest.main.model.data.AbstractModel}
 */
public class ElasticRepositoryImpl<T extends AbstractModel> implements ElasticRepository<T> {
	
    /**
     * The name of the ElasticSearch index alias
     */
    private String index;
    /**
     * The name of the ElasticSearch mapping created for the passed model. This will be
     * inferred from the generic type instantiated
     */
	private String mapping;
	/**
	 * The ElasticSearch client used for transport operations with the Elastic nodes
	 */
    private Client client;
    /**
     * The type inferred from the generic model used for this repository
     */
    private Class<?> genericType;
    /**
     * A list of fields inferred from the generic model from which this repository is built
     */
    private List<DataField> fields;
    /**
     * A factory required to instantiate new instances of the generic type
     */
	private ModelFactory<T> factory;
	
	/**
	 * Instantiate a new Elastic Repository instance
	 * if the index doesn't exist, it will be created
	 * @param factory The factory method which can be used to generate new instances 
	 * of the model being accessed by this repository   
	 * @param client The ElasticSearch client through which to perform all data operations
	 * @param migrationManagerNotifier The migration manager
	 * @param props Custom properties for this class
	 * @param retryTemplate The retry template for calls to the repository
	 * @throws IntrospectionException An error occurred while reflecting the given type in order to load its fields
	 * @throws IndexInitialisationException Index initialisation failed
	 * @throws MappingInitialisationException Mapping initialisation failed
	 * @throws IOException Thrown if the JSON builder fails to create JSON
	 */
	public ElasticRepositoryImpl(ModelFactory<T> factory, Client client) {
		this.factory = factory;
		this.index = "candidatetest";
		this.client = client;
		this.genericType = GenericTypeResolver.resolveTypeArgument(this.getClass(), ElasticRepository.class);
		this.mapping = getMappingName(this.genericType);
		initialise();
	}

	/**
	 * Reflect the fields defined on the type used by this repository and create an index 
	 * and mapping if they don't already exist
	 */
    protected void initialise () {
		try {
			this.fields = DataField.readFields(this.genericType);
			
			// Create the index
			if (!initialiseIndex(this.index)) {
				throw new RuntimeException("Index initialisation failed");
			}
			
			// Create/Update the mappings
			if (!initialiseMapping(this.index)) {
				throw new RuntimeException("Mapping initialisation failed");
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
    /**
     * Check whether this repository's index exists.
     *
     * @param index the index to check
     * @return {@code true} if the index exists
     */
	private Boolean indexExists(String index) {
		IndicesExistsResponse response = client.admin().indices().prepareExists(index).execute().actionGet();
		return response.isExists();
	}

	/**
	 * Generate a name for the Elastic mapping based on the passed type
	 * @param type The type of the mapping
	 * @return A string to be used to generate the mapping
	 */
	private String getMappingName(Class<?> type) {
		return type.getSimpleName().toLowerCase();
	}
	
	/**
	 * Create the index if it doesn't already exist.
	 *
	 * @param alias the alias that should point to index
	 * @param index the real index name
	 * @return {@code true} if the index is ready for access
	 */
	protected Boolean initialiseIndex(String index) {
    	if (!indexExists(index)) {
    		// Real index creation
    		return client.admin().indices().prepareCreate(index).execute().actionGet().isAcknowledged();
    	} else {
    		return true;
    	}
	}

    /**
     * Gets this repository's mapping metadata
     * @param realIndex Actual index to check for mappings
     * @return the mapping or null if it does not exist
     * @throws IOException the mapping is invalid
     */
	private Map<String, Object> getMapping() throws IOException {
		GetMappingsResponse response = client.admin().indices().prepareGetMappings(this.index).execute().actionGet();
		if (response.mappings().get(this.index).containsKey(mapping)) {
			return response.mappings().get(this.index).get(mapping).getSourceAsMap();
		} else {
			return null;
		}
	}
	
	/**
	 * Initialise mapping over a specific real index (must not be an alias).
	 *
	 * @param realIndex the real index
	 * @return the boolean
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IntrospectionException the introspection exception
	 */
	@SuppressWarnings("unchecked")
	protected Boolean initialiseMapping(String realIndex) throws IOException, IntrospectionException {
		Map<String, Object> existingMapping = getMapping();
    	boolean applyMapping = existingMapping == null;

		// Prepare to create the mapping
		PutMappingRequestBuilder builder = client.admin().indices().preparePutMapping(realIndex);
        builder.setType(mapping);

        // Start the mapping object
    	XContentBuilder source = XContentFactory.jsonBuilder().startObject();
    	Map<Object,Object> properties = null;
    	
    	if (existingMapping != null && existingMapping.get("properties") instanceof Map) {        		
    		properties = (Map<Object, Object>) existingMapping.get("properties");        		
    	}
    	
    	// Map the properties of this type
        source.startObject("properties");
        for (DataField field : fields) {
        	if (properties == null || !properties.containsKey(field.getName())) {
        		applyMapping = true;
        		addFieldMapping(field, source);
        	}	        	
        }
        builder.setSource(source.endObject().endObject());			
	    Boolean result = !applyMapping || builder.execute().actionGet().isAcknowledged();

	    // Seed data only if mapping did not previously exist
	    if (existingMapping == null) {
	    	seedData();
	    }
	    
	    return result;
	}
	
	/**
	 * @param field field to add mapping for
	 * @param source JSON builder
	 * @throws IOException when fails to create the JSON 
	 * @throws IntrospectionException Thrown when introspection fails
	 */
	private void addFieldMapping(DataField field, XContentBuilder source) throws IOException, IntrospectionException {
		// Start new field definition object
		source.startObject(field.getName());

		source.field("type", getESType(field.getType()));
		
		// Finish the object
		source.endObject();
	}

	/**
	 * This does nothing in the base class, but is called when a new mapping is created
	 * allowing inheriting classes to seed their data for the new mapping if required
	 */
	protected void seedData() {}
	
	/**
	 * Return an ElasticSearch type text from the field type
	 * @param type The type of field to convert
	 * @return A string as used in the mapping definition of ElasticSearch
	 */
	private String getESType(FieldType type) {
		switch (type) {
			case TEXT: return "keyword";
			case LONG: return "long";
			case INTEGER: return "integer";
			case SHORT: return "short";
			case BYTE: return "byte";
			case DOUBLE: return "double";
			case FLOAT: return "float";
			case BOOLEAN: return "boolean";
			case DATE: return "date";
			default: return "keyword";
		}
	}
		
	/**
	 * Create a {@link Map} representation of the passed POJO 
	 * @param entity The model whose fields you wish to turn into a {@link Map}
	 * @return A {@link Map} keyed by field name with the values of the passed model
	 */
	private Map<String, Object> mapFromEntity(T entity) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			for (DataField f : this.fields) {
				String name = f.getName();
				map.put(name, f.serialize(f.getGetter().invoke(entity)));
			}
	        return map;
		}
	    catch (Exception ex) {
	    	throw new RuntimeException(ex);
	    }
	}
	
	/**
	 * Create a new instance of the Generic Type from which this repository was created
	 * and populate it using a map
	 * @param map The {@link Map} containing field names and the values to be set
	 * @return A new instance of the generic type from which this repository is built
	 */
	private T entityFromMap(Map<String, Object> map) {
		try {
			T entity = this.factory.construct();
	        PropertyAccessor acc = PropertyAccessorFactory.forDirectFieldAccess(entity);
	        for (DataField f : this.fields) {
	        	if (f.isWritable()) {
					String name = f.getName();
					if (map.containsKey(name)) {
						acc.setPropertyValue(name, f.deserialize(map.get(name)));				
					}
	        	}
			}
	        return entity;
		}
        catch (Exception ex) {
        	throw new RuntimeException(ex);
        }
	}

	/**
	 * @see candidatetest.main.repository.ElasticRepository#create(candidatetest.main.model.data.AbstractModel)
	 */
	@Override
	public void createOne(T entity) {
		createMany(Arrays.asList(entity));
	}
	
	/**
	 * @see candidatetest.main.repository.ElasticRepository#create(candidatetest.main.model.data.AbstractModel)
	 */
	@Override
	public void createMany(List<T> entities) {
		if (entities != null && entities.size() > 0) {
			BulkRequestBuilder builder = client.prepareBulk();
			for (T entity : entities) {
				IndexRequestBuilder indexBuilder = client.prepareIndex(index, mapping).setSource(mapFromEntity(entity));
				indexBuilder.setOpType(OpType.INDEX);
				if (entity.getId() != null) {
					indexBuilder.setId(entity.getId());
				}
				builder.add(indexBuilder);
			}
			builder.execute();
		}
	}

	/**
	 * @see candidatetest.main.repository.ElasticRepository#findOne(java.lang.String)
	 */
	@Override
	public T findOne(String id) {
		try {
			SearchHits hits = client.prepareSearch(index)
				.setVersion(true)
		        .setTypes(mapping)
		        .setQuery(QueryBuilders.idsQuery().addIds(new String[] {id}))
		        .setFrom(0)
		        .setSize(1)
		        .get()
		        .getHits();
			return hits.totalHits == 0 ? null : entityFromMap(hits.getAt(0).getSourceAsMap());
		} catch (Exception ex) {
			throw new RuntimeException();
		}
	}

}
