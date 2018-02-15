package candidatetest.main.repository;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;

/**
 * A field definition used when reflecting models and creating data repositories
 */
public class DataField {
	
	/**
	 * The name of the field
	 */
	private String name;
	/**
	 * The type of the field
	 */
	private FieldType type;
	/**
	 * The class of the field
	 */
	private Class<?> fieldClass;
	/**
	 * Set to true if the field is an array of the specified type
	 */
	private boolean isArray;
	/**
	 * The method to get the value
	 */
	private Method getter;
	/**
	 * The method to set the value
	 */
	private String setterFunction;
	
	/**
	 * Construct a new field with 
	 * @param name The name of the field
	 * @param fieldClass The type of the field
	 * @param getter The method to get the value
	 * @param setterFunction The method to set the value
	 */
	public DataField(String name, Class<?> fieldClass, Method getter, String setterFunction) {
		String javaType = fieldClass.getSimpleName();
		this.name = name;
		this.fieldClass = fieldClass;
		this.isArray = javaType.endsWith("[]");
		this.getter = getter;
		this.setterFunction = setterFunction;
		if (this.isArray) {
			javaType = javaType.substring(0, javaType.length() - 2);
			this.fieldClass = this.fieldClass.getComponentType();
		}
		if (this.fieldClass.isEnum()) {
			this.type = FieldType.ENUM;
		} else {
			this.type = parseType(javaType);
		}
	}
	
	/**
	 * Supported types for fields in Java
	 */
	public enum FieldType {
		/** Text */ 					TEXT,
		/** 64 bit integer */ 			LONG,
		/** 32 bit integer */ 			INTEGER,
		/** 16 bit integer */ 			SHORT,
		/** 8 bit integer */ 			BYTE,
		/** 64 bit floating point */ 	DOUBLE,
		/** 32 bit floating point */ 	FLOAT,
		/** BigDecimal */				DECIMAL,
		/** True/False */ 				BOOLEAN,
		/** Date */ 					DATE,
		/** Enumeration */				ENUM,
		/** Unrecognised */ 			UNKNOWN
	}
	
	/**
	 * Convert a Java simple type name to a {@link FieldType} 
	 * @param javaType The simple type name of the Field (with array decoration removed) 
	 * @return A {@link FieldType} enumeration value.  If the type is not recognised UNKNOWN is used
	 */
	private FieldType parseType(String javaType) {
		
		switch (javaType.toLowerCase()) {
			case "string" : return FieldType.TEXT;
			case "long" : return FieldType.LONG;
			case "int" : return FieldType.INTEGER;
			case "short" : return FieldType.SHORT;
			case "byte" : return FieldType.BYTE;
			case "double" : return FieldType.DOUBLE;
			case "float" : return FieldType.FLOAT;
			case "bigdecimal" : return FieldType.DECIMAL; 
			case "boolean" : return FieldType.BOOLEAN;
			case "offsetdatetime" : return FieldType.DATE;
			default:
				return FieldType.UNKNOWN;
		}

	}
	
	/**
	 * Get the name of the field
	 * @return The String name of the field
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the type of the field
	 * @return The general type of the field
	 */
	public FieldType getType() {
		return this.type;
	}
	
	/**
	 * Get a flag indicating whether the field is an array
	 * @return {@code true} if the field is an array
	 */
	public boolean getIsArray() {
		return this.isArray;
	}
	
	/**
	 * Get the class of the associated type
	 * @return the class of the associated type
	 */
	public Class<?> getFieldClass() {
		return this.fieldClass;
	}
	
	/**
	 * Set the class of the associated type
	 * @param fieldClass The class of the associated type
	 */
	public void setFieldClass(Class<?> fieldClass) {
		this.fieldClass = fieldClass;
	}

	/**
	 * Indicates that the field is writable
	 * @return a boolean value indicating that the field is writable
	 */
	public boolean isWritable() {
		return this.setterFunction != null;
	}

	/**
	 * Get the value of the getter property
	 * @return a Method value indicating the value of the getter property
	 */
	public Method getGetter() {
		return getter;
	}

	/**
	 * Set the value of the getter property
	 * @param getter a Method indicating the value of the getter property
	 */
	public void setGetter(Method getter) {
		this.getter = getter;
	}

	/**
	 * Get the value of the setter property
	 * @return a Method value indicating the value of the setter property
	 */
	public String getSetter() {
		return setterFunction;
	}

	/**
	 * Set the value of the setter property
	 * @param setterFunction a Method indicating the value of the setter property
	 */
	public void setSetter(String setterFunction) {
		this.setterFunction = setterFunction;
	}

	
	/**
	 * Parse the given field for sending the data to store
	 * @param input The field to convert
	 * @return An object suitable for including in an elastic map
	 * @throws IntrospectionException Thrown when introspection fails
	 * @throws BeansException Thrown when beans fails
	 */
	public Object serialize(Object input) throws BeansException, IntrospectionException {
		if (input == null) {
			return input;
		} else if (this.type == FieldType.ENUM) {
			return ((Enum<?>)input).name();
		} else if (this.type == FieldType.DECIMAL) {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("decimal", input);
			map.put("value", ((BigDecimal)input).doubleValue());
			return map;
		} else {
			return input;
		}
	}
	
	/**
	 * Parse the given field when receiving the data from store
	 * @param input A field to parse, either a hashmap or a field value
	 * @return An object which can be cast to the expected type
	 * @throws ParseException Thrown if the date is not successfully parsed
	 * using the format defined in the application properties
	 * @throws SecurityException Thrown if reflection fails to access the 
	 * class of this field 
	 * @throws NoSuchMethodException Thrown if the reflection can't find the enumeration of this field 
	 * @throws InvocationTargetException Thrown if the enumeration couldn't be invoked 
	 * @throws IllegalArgumentException Thrown if the enumeration doesn't implement valueOf
	 * @throws IllegalAccessException Thrown if valueOf couldn't be accessed
	 * @throws IntrospectionException Thrown when introspection fails
	 * @throws InstantiationException Thrown when instantiation fails
	 */
	@SuppressWarnings("unchecked")
	public Object deserialize(Object input) throws ParseException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException, InstantiationException {
		if (input == null) {
			return null;
		} else if (this.type == FieldType.DATE) {
			String dateString = (String)input;
			return OffsetDateTime.parse(dateString);
		} else if (this.type == FieldType.ENUM) {
			Method valueOf = fieldClass.getMethod("valueOf", String.class);
			Object value = valueOf.invoke(null, input);
			return value;
		} else if (this.type == FieldType.DECIMAL) {
			if (!(input instanceof Map)) {
				return null;																																																																										
			} else {
				Map<String, Object> map = (Map<String, Object>) input;
				Object decimal = map.get("decimal");
				if (decimal != null && decimal instanceof String) {
					return new BigDecimal((String)decimal);
				} else {
					return null;
				}
			}
		} else {
			return input;
		}
	}
	
	
	/**
	 * Reflect an array of {@link DataField} from the model on which this
	 * repository is based
	 * @param type The type whose fields should be returned
	 * @return A list of {@link DataField} relating to the model on which this
	 * instance is based
	 * @throws IntrospectionException Thrown if reflection failed and the fields could not
	 * be determined
	 */
	public static ArrayList<DataField> readFields(Class<?> type) throws IntrospectionException {
		final BeanInfo beanInfo = Introspector.getBeanInfo(type);
		ArrayList<DataField> fields = new ArrayList<DataField>();
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
			// If there is a read method and the read method is not annotated as read-only it should be included here
			if (descriptor.getReadMethod() != null) {
				loadField(fields, descriptor.getName(), descriptor.getReadMethod(), descriptor.getName());
			}
		}
		
		return fields;
	}
	
	/**
	 * Load the field into the passed array
	 * @param fields The field collection to add the field to
	 * @param name The name of the field to be used in the data store
	 * @param getter The getter method
	 * @param setterFunction The setter method
	 */
	private static void loadField(ArrayList<DataField> fields, String name, Method getter, String setterFunction) {
		if (getter != null) {
			DataField field = new DataField(name, getter.getReturnType(), getter, setterFunction);
			if (!field.getName().equals("id") && field.getType() != FieldType.UNKNOWN) {
				fields.add(field);
			}
		}
	}
}

