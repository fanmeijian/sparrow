package cn.sparrowmini.owl.solr.model;
import org.springframework.lang.Nullable;

/**
 * Defines a Field that can be used within {@link Criteria}.
 *
 * @author Christoph Strobl
 */
public interface Field {

    /**
     * Get the name of the field used in {@code schema.xml} of solr server
     *
     * @return
     */
    @Nullable
    String getName();

}