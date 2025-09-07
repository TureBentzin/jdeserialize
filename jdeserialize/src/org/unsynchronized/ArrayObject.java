package org.unsynchronized;

/**
 * <p>Represents an array instance, including the values the comprise the array.  </p>
 *
 * <p>Note that in arrays of primitives, the classdesc will be named "[x", where x is the
 * field type code representing the primitive type.  See jdeserialize.resolveJavaType()
 * for an example of analysis/generation of human-readable names from these class names.</p>
 */
public class ArrayObject extends Content {
    /**
     * Type of the array instance.
     */
    public ClassDescriptor ClassDescriptor;

    /**
     * Values of the array, in the order they were read from the stream.
     */
    public ObjectList data;

    public ArrayObject(int handle, ClassDescriptor cd, ObjectList data) {
        super(ContentType.ARRAY);
        this.handle = handle;
        this.ClassDescriptor = cd;
        this.data = data;
    }

    public String toString() {
        return "[Array " + JDeserialize.hex(handle) + " classdesc " + ClassDescriptor.toString() + ": "
                + data.toString() + "]";
    }
}

