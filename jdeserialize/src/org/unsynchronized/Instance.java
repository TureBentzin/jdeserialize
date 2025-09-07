package org.unsynchronized;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an instance of a non-enum, non-Class, non-ObjectStreamClass, 
 * non-array class, including the non-transient field values, for all classes in its
 * hierarchy and inner classes.
 */
public class Instance extends Content {
    /**
     * Collection of field data, organized by class description.  
     */
    public Map<ClassDescriptor, Map<Field, Object>> fieldData;

    /**
     * Class description for this instance.
     */
    public ClassDescriptor classDescriptor;

    /**
     * Constructor.
     */
    public Instance() {
        super(ContentType.INSTANCE);
        this.fieldData = new HashMap<>();
    }

    public String toString() {
        return classDescriptor.name + ' ' + "_h" + JDeserialize.hex(handle) +
                " = r_" + JDeserialize.hex(classDescriptor.handle) + ";  ";
    }

    /**
     * Object annotation data.
     */
    public Map<ClassDescriptor, List<IContent>> annotations;
}
