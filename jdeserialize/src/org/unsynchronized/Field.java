package org.unsynchronized;

import java.io.*;

/**
 * This class represents a field within a class description/declaration (classdesc).  It
 * contains information about the type and name of the field.  Fields themselves don't
 * have a handle; inside the stream, they exist only as part of a class description.
 */
public class Field {
    /**
     * The type of the field.
     */
    public FieldType type;

    /**
     * The name of the field.
     */
    public String name;

    /**
     * The string object representing the class name.
     */
    public StringObject className;

    private boolean isInnerClassReference = false;

    /**
     * Tells whether or not this class is an inner class reference.  This value is set by
     * connectMemberClasses() -- if this hasn't been called, or if the field hasn't been
     * otherwise set by setIsInnerClassReference(), it will be false;
     *
     * @return true if the class is an inner class reference
     */
    public boolean isInnerClassReference() {
        return isInnerClassReference;
    }

    /**
     * Sets the flag that denotes whether this class is an inner class reference.
     *
     * @param nis the value to set; true iff the class is an inner class reference.
     */
    public void setIsInnerClassReference(boolean nis) {
        this.isInnerClassReference = nis;
    }

    /**
     * Constructor.  
     *
     * @param type the field type
     * @param name the field name
     * @param className the class name
     */
    public Field(FieldType type, String name, StringObject className) throws ValidityException {
        this.type = type;
        this.name = name;
        this.className = className;
        if (className != null) {
            validate(className.value);
        }
    }

    /**
     * Constructor for simple fields.
     *
     * @param type the field type
     * @param name the field name
     */
    public Field(FieldType type, String name) throws ValidityException {
        this(type, name, null);
    }

    /**
     * Get a string representing the type for this field in Java (the language)
     * format.
     * @return a string representing the fully-qualified type of the field
     * @throws IOException if a validity or I/O error occurs
     */
    public String getJavaType() throws IOException {
        return JDeserialize.resolveJavaType(this.type, this.className == null ? null : this.className.value, true, false);
    }

    /**
     * Changes the name of an object reference to the name specified.  This is used by
     * the inner-class-connection code to fix up field references.
     * @param name the fully qualified class
     * @throws ValidityException if the field isn't a reference type, or another
     * validity error occurs
     */
    public void setReferenceTypeName(String name) throws ValidityException {
        if (this.type != FieldType.OBJECT) {
            throw new ValidityException("can't fix up a non-reference field!");
        }
        this.className.value = "L" + name.replace('.', '/') + ";";
    }

    /**
     * Validates the class name for an object field type.
     *
     * @param name the class name to validate
     * @throws ValidityException if the class name is invalid
     */
    public void validate(String name) throws ValidityException {
        if (this.type == FieldType.OBJECT) {
            if (name == null) {
                throw new ValidityException("classname can't be null");
            }
            if (name.charAt(0) != 'L') {
                throw new ValidityException("invalid object field type descriptor: " + className.value);
            }
            int end = name.indexOf(';');
            if (end == -1 || end != (name.length() - 1)) {
                throw new ValidityException("invalid object field type descriptor (must end with semicolon): " + className.value);
            }
        }
    }
}
