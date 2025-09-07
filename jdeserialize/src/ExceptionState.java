package org.unsynchronized;

/**
 * <p>
 * This object contains embedded information about a serialization that failed, throwing
 * an exception.  It includes the actual exception object (which was serialized by the
 * ObjectOutputStream) and the raw bytes of the stream data that was read before the
 * exception was recognized.
 * </p>
 *
 * <p>
 * For the mechanics of exception serialization, see the Object Serialization
 * Specification.
 * </p>
 */
public class ExceptionState extends Content {
    /**
     * The serialized exception object.
     */
    public IContent exception;

    /**
     * <p>
     * An array of bytes representing the data read before the exception was encountered.
     * Generally, this starts with the first "tc" byte (cf. protocol spec), which is an
     * ObjectStreamConstants value and ends with 0x78 (the tc byte corresponding to
     * TC_EXCEPTION).  However, this isn't guaranteed; it may include *more* data.  
     * </p>
     *
     * <p>
     * In other words, this is the incomplete object that was being written while the
     * exception was caught by the ObjectOutputStream.  It is not likely to be cleanly
     * parseable.
     * </p>
     *
     * <p>
     * The uncertainty centers around the fact that this data is gathered by jdeserialize
     * using a LoggerInputStream, and the underlying DataInputStream may have read more
     * than is necessary.  In all tests conducted so far, the above description is
     * accurate.
     * </p>
     */
    public byte[] data;

    /**
     * Consturctor.
     * @param exception the serialized exception object
     * @param data the array of stream bytes that led up to the exception
     */
    public ExceptionState(IContent exception, byte[] data) {
        super(ContentType.EXCEPTIONSTATE);
        this.exception = exception;
        this.data = data;
        this.handle = exception.getHandle();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ExceptionState object ").append(exception.toString()).append("  length ").append(data.length);
        if (data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                if ((i % 16) == 0) {
                    stringBuilder.append(JDeserialize.lineSeparator).append(String.format("%7x: ", i));
                }
                stringBuilder.append(" ").append(JDeserialize.hexnoprefix(data[i]));
            }
            stringBuilder.append(JDeserialize.lineSeparator);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
