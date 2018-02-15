package candidatetest.main.configuration;

import java.util.ArrayList;

import javax.servlet.Filter;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Initialise the dispatcher servlet for the REST framework
 */
public class CandidateTestInit extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * A list of servlet filters to be applied
     */
    private ArrayList<Filter> filters = new ArrayList<Filter>();

    /** Set the root configuration used for providing beans
     * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer#getRootConfigClasses()
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { CandidateTestConfig.class };
    }

    /** Return the servlet configuration classes
     * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer#getServletConfigClasses()
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return null;
    }

    /** Map the servlet
     * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer#getServletMappings()
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    /** Add the servlet filter chain
     * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer#getServletFilters()
     */
    @Override
    protected Filter[] getServletFilters() {
    	return filters.toArray(new Filter[] {});
    }

}
