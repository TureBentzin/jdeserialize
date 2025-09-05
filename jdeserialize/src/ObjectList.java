package org.unsynchronized;
import java.io.Serial;
import java.util.*;

/**
 * <p>Typed collection used for storing the values of a serialized array.  </p>
 *
 * <p>Primitive types are stored using their corresponding objects; for instance, an int is
 * stored as an Integer.  To determine whether or not this is an array of ints or of
 * Integer instances, check the name in the arrayobj's class description.</p>
 */
public class ObjectList extends ArrayList<Object> {
    @Serial
    private static final long serialVersionUID = 2277356908919248L;

    private final fieldtype fieldType;

    /**
     * Constructor.
     * @param ft field type of the array
     */
    public ObjectList(fieldtype ft) {
        super();
        this.fieldType = ft;
    }

    /**
     * Gets the field type of the array.
     *
     * @return the field type of the array
     */
    public fieldtype getFieldType() {
        return fieldType;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ObjectList size=").append(this.size());
        boolean first = true;
        for(Object o: this) {
            if(first) {
                first = false;
                sb.append(' ');
            } else {
                sb.append(", ");
            }
            sb.append(o.toString());
        }
        return sb.toString();
    }
}
