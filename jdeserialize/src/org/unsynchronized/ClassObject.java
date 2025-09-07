package org.unsynchronized;

/**
 * This represents a Class object (i.e. an instance of type Class) serialized in the
 * stream.
 */
public class ClassObject extends Content {
    /**
     * The class description, including its name.
     */
    public ClassDescriptor ClassDescriptor;

    /**
     * Constructor.
     *
     * @param handle the instance's handle
     * @param cd the instance's class description
     */
    public ClassObject(int handle, ClassDescriptor cd) {
        super(ContentType.CLASS);
        this.handle = handle;
        this.ClassDescriptor = cd;
    }
    public String toString() {
        return "[class " + JDeserialize.hex(handle) + ": " + ClassDescriptor.toString() + "]";
    }
}

