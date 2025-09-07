package org.unsynchronized;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/**
 * The main user-facing class for the jdeserialize library.  Also the implementation of
 * the command-line tool.<br/>
 * <br/>
 * Library:<br/>
 * <br/>
 * The jdeserialize class parses the stream (method run()).  From there, call the
 * getContent() method to get an itemized list of all items written to the stream, 
 * or getHandleMaps() to get a list of all handle->content maps generated during parsing.
 * The objects are generally instances that implement the interface "content"; see the
 * documentation of various implementors to get more information about the inner
 * representations.<br/>
 * <br/>
 * To enable debugging on stdout, use the enableDebug() or streamableDebug() options.   <br/> 
 * <br/>
 * <br/>
 * Command-line tool:   <br/>
 * <br/>
 * The tool reads in a set of files and generates configurable output on stdout.  The
 * primary output consists of three separate stages.  The first stage is  a textual 
 * description of every piece of content in the stream, in the order it was written.
 * There is generally a one-to-one mapping between ObjectOutputStream.writeXXX() calls and
 * items printed in the first stage.  The first stage may be suppressed with the
 * -nocontent command-line option. <br/>
 * <br/>
 * The second stage is a list of every class declaration serialized in the file.  These
 * are formatted as normal Java language class declarations.  Several options are
 * available to govern this stage, including -filter, -showarrays, -noclasses, and
 * -fixnames. <br/>
 * <br/>
 * The third stage is a dump of every instance embedded inside the stream, including
 * textual descriptions of field values.  This is useful for casual viewing of class data. 
 * To suppress this stage, use -noinstances. <br/>
 * <br/>
 * You can also get debugging information generated during the parse phase by supplying
 * -debug.
 * <br/>
 * The data from block data objects can be extracted with the -blockdata <file> option.
 * Additionally, a manifest describing the size of each individual block can be generated
 * with the -blockdatamanifest <file> option.
 * <br/>
 * References: <br/>
 *     - Java Object Serialization Specification ch. 6 (Object Serialization Stream
 *       Protocol): <br/>
 *       http://download.oracle.com/javase/6/docs/platform/serialization/spec/protocol.html <br/>
 *     - "Modified UTF-8 Strings" within the JNI specification: 
 *       http://download.oracle.com/javase/1.5.0/docs/guide/jni/spec/types.html#wp16542 <br/>
 *     - "Inner Classes Specification" within the JDK 1.1.8 docs:
 *       http://java.sun.com/products/archive/jdk/1.1/ <br/>
 *     - "Java Language Specification", third edition, particularly section 3:
 *       http://java.sun.com/docs/books/jls/third_edition/html/j3TOC.html <br/>
 *
 * @see IContent
 */
@SuppressWarnings("CallToPrintStackTrace")
public class JDeserialize implements Serializable {
    @Serial
    private static final long serialVersionUID = 78790714646095L;
    public static final String INDENT = "    ";
    @SuppressWarnings("SpellCheckingInspection")
    public static final int CODEWIDTH = 90;
    public static final String lineSeparator = System.lineSeparator();
    public static final String[] keywords = new String[]{
            "abstract", "continue", "for", "new", "switch", "assert", "default", "if",
            "package", "synchronized", "boolean", "do", "goto", "private", "this",
            "break", "double", "implements", "protected", "throw", "byte", "else",
            "import", "public", "throws", "case", "enum", "instanceof", "return",
            "transient", "catch", "extends", "int", "short", "try", "char", "final",
            "interface", "static", "void", "class", "finally", "long", "strictfp",
            "volatile", "const", "float", "native", "super", "while"};
    public static HashSet<String> keywordSet;

    private final HashMap<Integer, IContent> handles = new HashMap<>();
    private final ArrayList<Map<Integer, IContent>> handleMaps = new ArrayList<>();
    private ArrayList<IContent> IContent;
    private int currentHandle;
    private boolean debugEnabled;

    static {
        keywordSet = new HashSet<>();
        Collections.addAll(keywordSet, keywords);
    }

    /**
     * <p>
     * Retrieves the list of content objects that were written to the stream.  Each item
     * generally corresponds to an invocation of an ObjectOutputStream writeXXX() method.
     * A notable exception is the class exceptionstate, which represents an embedded
     * exception that was caught during serialization.
     * </p>
     *
     * <p>
     * See the various implementors of content to get information about what data is
     * available.  
     * </p>
     *
     * <p>
     * Entries in the list may be null, because it's perfectly legitimate to write a null
     * reference to the stream.  
     * </p>
     *
     * @return a list of content objects
     * @see IContent
     * @see exceptionstate
     */
    public List<IContent> getContent() {
        return IContent;
    }

    /**
     * <p>
     * Return a list of Maps containing every object with a handle.  The keys are integers
     * -- the handles themselves -- and the values are instances of type content.
     * </p>
     *
     * <p>
     * Although there is only one map active at a given point, a stream may have multiple
     * logical maps: when a reset happens (indicated by TC_RESET), the current map is
     * cleared.  
     * </p>
     *
     * <p>
     * See the spec for details on handles.
     * </p>
     * @return a list of <Integer,content> maps
     */
    public List<Map<Integer, IContent>> getHandleMaps() {
        return handleMaps;
    }

    /**
     * Suitably escapes non-printable-ASCII characters (and doublequotes) for use 
     * in a Java string literal.
     *
     * @param str string to escape
     * @return an escaped version of the string
     */
    public static String unicodeEscape(String str) {
        StringBuilder sb = new StringBuilder();
        int cpLength = str.codePointCount(0, str.length());
        for (int i = 0; i < cpLength; i++) {
            int cp = str.codePointAt(i);
            if (cp == '"') {
                sb.append("\\\"");
            }
            if (cp < 0x20 || cp > 0x7f) {
                sb.append("\\u").append(hexnoprefix(4));
            } else {
                sb.appendCodePoint(cp);
            }
        }
        return sb.toString();
    }

    public static String indent(int level) {
        return INDENT.repeat(Math.max(0, level));
    }

    public void readClassData(DataInputStream stream, Instance inst) throws IOException {
        ArrayList<classdesc> classes = new ArrayList<>();
        inst.classdesc.getHierarchy(classes);
        Map<classdesc, Map<field, Object>> allData = new HashMap<>();
        Map<classdesc, List<IContent>> ann = new HashMap<>();
        for (classdesc cd : classes) {
            Map<field, Object> values = new HashMap<>();
            if ((cd.descflags & ObjectStreamConstants.SC_SERIALIZABLE) != 0) {
                if ((cd.descflags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0) {
                    throw new IOException("SC_EXTERNALIZABLE & SC_SERIALIZABLE encountered");
                }
                for (field f : cd.fields) {
                    Object o = readFieldValue(f.type, stream);
                    values.put(f, o);
                }
                allData.put(cd, values);
                if ((cd.descflags & ObjectStreamConstants.SC_WRITE_METHOD) != 0) {
                    if ((cd.descflags & ObjectStreamConstants.SC_ENUM) != 0) {
                        throw new IOException("SC_ENUM & SC_WRITE_METHOD encountered!");
                    }
                    ann.put(cd, read_classAnnotation(stream));
                }
            } else if ((cd.descflags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0) {
                if ((cd.descflags & ObjectStreamConstants.SC_SERIALIZABLE) != 0) {
                    throw new IOException("SC_SERIALIZABLE & SC_EXTERNALIZABLE encountered");
                }
                if ((cd.descflags & ObjectStreamConstants.SC_BLOCK_DATA) != 0) {
                    throw new EOFException("hit externalizable with nonzero SC_BLOCK_DATA; can't interpret data");
                } else {
                    ann.put(cd, read_classAnnotation(stream));
                }
            }
        }
        inst.annotations = ann;
        inst.fielddata = allData;
    }

    public Object readFieldValue(FieldType fieldType, DataInputStream stream) throws IOException {
        switch (fieldType) {
            case BYTE:
                return stream.readByte();
            case CHAR:
                return stream.readChar();
            case DOUBLE:
                return stream.readDouble();
            case FLOAT:
                return stream.readFloat();
            case INTEGER:
                return stream.readInt();
            case LONG:
                return stream.readLong();
            case SHORT:
                return stream.readShort();
            case BOOLEAN:
                return stream.readBoolean();
            case OBJECT:
            case ARRAY:
                byte stc = stream.readByte();
                if (fieldType == FieldType.ARRAY && stc != ObjectStreamConstants.TC_ARRAY) {
                    throw new IOException("array type listed, but typecode is not TC_ARRAY: " + hex(stc));
                }
                IContent c = readContent(stc, stream, false);
                if (c != null && c.isExceptionObject()) {
                    throw new ExceptionReadException(c);
                }
                return c;
            default:
                throw new IOException("can't process type: " + fieldType);
        }
    }

    private int newHandle() {
        return currentHandle++;
    }

    public static String resolveJavaType(FieldType type, String classname, boolean convertSlashes, boolean fixName) throws IOException {
        if (type == FieldType.ARRAY) {
            StringBuilder asb = new StringBuilder();
            for (int i = 0; i < classname.length(); i++) {
                char ch = classname.charAt(i);
                switch (ch) {
                    case '[':
                        asb.append("[]");
                        continue;
                    case 'L':
                        String cn = decodeClassName(classname.substring(i), convertSlashes);
                        if (fixName) {
                            cn = fixClassName(cn);
                        }
                        return cn + asb;
                    default:
                        if (ch < 1 || ch > 127) {
                            throw new ValidityException("invalid array field type descriptor character: " + classname);
                        }
                        FieldType ft = FieldType.get((byte) ch);
                        if (i != (classname.length() - 1)) {
                            throw new ValidityException("array field type descriptor is too long: " + classname);
                        }
                        String ftn = ft.getJavaType();
                        if (fixName) {
                            ftn = fixClassName(ftn);
                        }
                        return ftn + asb;
                }
            }
            throw new ValidityException("array field type descriptor is too short: " + classname);
        } else if (type == FieldType.OBJECT) {
            return decodeClassName(classname, convertSlashes);
        } else {
            return type.getJavaType();
        }
    }

    public List<IContent> read_classAnnotation(DataInputStream stream) throws IOException {
        List<IContent> list = new ArrayList<>();
        while (true) {
            byte tc = stream.readByte();
            if (tc == ObjectStreamConstants.TC_ENDBLOCKDATA) {
                return list;
            }
            if (tc == ObjectStreamConstants.TC_RESET) {
                reset();
                continue;
            }
            IContent c = readContent(tc, stream, true);
            if (c != null && c.isExceptionObject()) {
                throw new ExceptionReadException(c);
            }
            list.add(c);
        }
    }

    public static void dump_Instance(Instance inst, PrintStream ps) {
        StringBuffer sb = new StringBuffer();
        sb.append("[instance ").append(hex(inst.handle)).append(": ").append(hex(inst.classdesc.handle)).append("/").append(inst.classdesc.name);
        if (inst.annotations != null && !inst.annotations.isEmpty()) {
            sb.append(lineSeparator).append("  object annotations:").append(lineSeparator);
            for (classdesc cd : inst.annotations.keySet()) {
                sb.append("    ").append(cd.name).append(lineSeparator);
                for (IContent c : inst.annotations.get(cd)) {
                    sb.append("        ").append(c.toString()).append(lineSeparator);
                }
            }
        }
        if (inst.fielddata != null && !inst.fielddata.isEmpty()) {
            sb.append(lineSeparator).append("  field data:").append(lineSeparator);
            for (classdesc cd : inst.fielddata.keySet()) {
                sb.append("    ").append(hex(cd.handle)).append("/").append(cd.name).append(":").append(lineSeparator);
                for (field f : inst.fielddata.get(cd).keySet()) {
                    Object o = inst.fielddata.get(cd).get(f);
                    sb.append("        ").append(f.name).append(": ");
                    if (o instanceof IContent c) {
                        int h = c.getHandle();
                        if (h == inst.handle) {
                            sb.append("this");
                        } else {
                            sb.append("r").append(hex(h));
                        }
                        sb.append(": ").append(c);
                        sb.append(lineSeparator);
                    } else {
                        sb.append(o).append(lineSeparator);
                    }
                }
            }
        }
        sb.append("]");
        ps.println(sb);
    }

    /**
     * "Fix" the given name by transforming illegal characters, such that the end result
     * is a legal Java identifier that is not a keyword.  
     * If the string is modified at all, the result will be prepended with "$__".
     *
     * @param name the name to be transformed
     * @return the unmodified string if it is legal, otherwise a legal-identifier version
     */
    public static String fixClassName(String name) {
        if (name == null) {
            return "$__null";
        }
        if (keywordSet.contains(name)) {
            return "$__" + name;
        }
        StringBuilder sb = new StringBuilder();
        int cpLength = name.codePointCount(0, name.length());
        if (cpLength < 1) {
            return "$__zerolen";
        }
        boolean modified = false;
        int scp = name.codePointAt(0);
        if (!Character.isJavaIdentifierStart(scp)) {
            modified = true;
            if (!Character.isJavaIdentifierPart(scp) || Character.isIdentifierIgnorable(scp)) {
                sb.append("x");
            } else {
                sb.appendCodePoint(scp);
            }
        } else {
            sb.appendCodePoint(scp);
        }

        for (int i = 1; i < cpLength; i++) {
            int cp = name.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp) || Character.isIdentifierIgnorable(cp)) {
                modified = true;
                sb.append("x");
            } else {
                sb.appendCodePoint(cp);
            }
        }
        if (modified) {
            return "$__" + sb;
        } else {
            return name;
        }
    }

    public static void dumpClassDesc(int indentLevel, classdesc classdesc, PrintStream ps, boolean fixName) throws IOException {
        String classname = classdesc.name;
        if (fixName) {
            classname = fixClassName(classname);
        }
        if (classdesc.annotations != null && !classdesc.annotations.isEmpty()) {
            ps.println(indent(indentLevel) + "// annotations: ");
            for (IContent content : classdesc.annotations) {
                ps.print(indent(indentLevel) + "// " + indent(1));
                ps.println(content.toString());
            }
        }
        if (classdesc.classtype == classdesctype.NORMALCLASS) {
            if ((classdesc.descflags & ObjectStreamConstants.SC_ENUM) != 0) {
                ps.print(indent(indentLevel) + "enum " + classname + " {");
                boolean shouldIndent = true;
                int length = indent(indentLevel + 1).length();
                for (String enumConstant : classdesc.enumconstants) {
                    if (shouldIndent) {
                        ps.println();
                        ps.print(indent(indentLevel + 1));
                        shouldIndent = false;
                    }
                    length += enumConstant.length();
                    ps.print(enumConstant + ", ");
                    if (length >= CODEWIDTH) {
                        length = indent(indentLevel + 1).length();
                        shouldIndent = true;
                    }
                }
                ps.println();
                ps.println(indent(indentLevel) + "}");
                return;
            }
            ps.print(indent(indentLevel));
            if (classdesc.isStaticMemberClass()) {
                ps.print("static ");
            }
            ps.print("class " + (classname.charAt(0) == '[' ? resolveJavaType(FieldType.ARRAY, classdesc.name, false, fixName) : classname));
            if (classdesc.superclass != null) {
                ps.print(" extends " + classdesc.superclass.name);
            }
            ps.print(" implements ");
            if ((classdesc.descflags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0) {
                ps.print("java.io.Externalizable");
            } else {
                ps.print("java.io.Serializable");
            }
            if (classdesc.interfaces != null) {
                for (String intf : classdesc.interfaces) {
                    ps.print(", " + intf);
                }
            }
            ps.println(" {");
            for (field f : classdesc.fields) {
                if (f.isInnerClassReference()) {
                    continue;
                }
                ps.print(indent(indentLevel + 1) + f.getJavaType());
                ps.println(" " + f.name + ";");
            }
            for (classdesc icd : classdesc.innerclasses) {
                dumpClassDesc(indentLevel + 1, icd, ps, fixName);
            }
            ps.println(indent(indentLevel) + "}");
        } else if (classdesc.classtype == classdesctype.PROXYCLASS) {
            ps.print(indent(indentLevel) + "// proxy class " + hex(classdesc.handle));
            if (classdesc.superclass != null) {
                ps.print(" extends " + classdesc.superclass.name);
            }
            ps.println(" implements ");
            for (String intf : classdesc.interfaces) {
                ps.println(indent(indentLevel) + "//    " + intf + ", ");
            }
            if ((classdesc.descflags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0) {
                ps.println(indent(indentLevel) + "//    java.io.Externalizable");
            } else {
                ps.println(indent(indentLevel) + "//    java.io.Serializable");
            }
        } else {
            throw new ValidityException("encountered invalid classdesc type!");
        }
    }

    public void setHandle(int handle, IContent c) throws IOException {
        if (handles.containsKey(handle)) {
            throw new IOException("trying to reset handle " + hex(handle));
        }
        handles.put(handle, c);
    }

    public void reset() {
        debug("reset ordered!");
        if (!handles.isEmpty()) {
            HashMap<Integer, IContent> hm = new HashMap<>(handles);
            handleMaps.add(hm);
        }
        handles.clear();
        currentHandle = ObjectStreamConstants.baseWireHandle;  // 0x7e0000
    }

    /**
     * Read the content of a thrown exception object.  According to the spec, this must be
     * an object of type Throwable.  Although the Sun JDK always appears to provide enough
     * information about the hierarchy to reach all the way back to java.lang.Throwable,
     * it's unclear whether this is actually a requirement.  From my reading, it's
     * possible that some other ObjectOutputStream implementations may leave some gaps in
     * the hierarchy, forcing this app to hit the classloader.  To avoid this, we merely
     * ensure that the written object is indeed an instance; ensuring that the object is
     * indeed a Throwable is an exercise left to the user.
     */
    public IContent readException(DataInputStream stream) throws IOException {
        reset();
        byte tc = stream.readByte();
        if (tc == ObjectStreamConstants.TC_RESET) {
            throw new ValidityException("TC_RESET for object while reading exception: what should we do?");
        }
        IContent c = readContent(tc, stream, false);
        if (c == null) {
            throw new ValidityException("stream signaled for an exception, but exception object was null!");
        }
        if (!(c instanceof Instance)) {
            throw new ValidityException("stream signaled for an exception, but content is not an object!");
        }
        if (c.isExceptionObject()) {
            throw new ExceptionReadException(c);
        }
        c.setIsExceptionObject(true);
        reset();
        return c;
    }

    public classdesc readClassDesc(DataInputStream stream) throws IOException {
        byte tc = stream.readByte();
        return handleClassDesc(tc, stream, false);
    }

    public classdesc readNewClassDesc(DataInputStream stream) throws IOException {
        byte tc = stream.readByte();
        return handleNewClassDesc(tc, stream);
    }

    public IContent readPrevObject(DataInputStream stream) throws IOException {
        int handle = stream.readInt();
        if (!handles.containsKey(handle)) {
            throw new ValidityException("can't find an entry for handle " + hex(handle));
        }
        IContent content = handles.get(handle);
        debug("prevObject: handle " + hex(content.getHandle()) + " classdesc " + content);
        return content;
    }

    public classdesc handleNewClassDesc(byte tc, DataInputStream stream) throws IOException {
        return handleClassDesc(tc, stream, true);
    }

    public classdesc handleClassDesc(byte tc, DataInputStream stream, boolean mustBeNew) throws IOException {
        if (tc == ObjectStreamConstants.TC_CLASSDESC) {
            String name = stream.readUTF();
            long serialVersionUID = stream.readLong();
            int handle = newHandle();
            byte descflags = stream.readByte();
            short fieldCount = stream.readShort();
            if (fieldCount < 0) {
                throw new IOException("invalid field count: " + fieldCount);
            }
            field[] fields = new field[fieldCount];
            for (short s = 0; s < fieldCount; s++) {
                byte fieldType = stream.readByte();
                if (fieldType == 'B' || fieldType == 'C' || fieldType == 'D'
                        || fieldType == 'F' || fieldType == 'I' || fieldType == 'J'
                        || fieldType == 'S' || fieldType == 'Z') {
                    String fieldName = stream.readUTF();
                    fields[s] = new field(FieldType.get(fieldType), fieldName);
                } else if (fieldType == '[' || fieldType == 'L') {
                    String fieldName = stream.readUTF();
                    byte stc = stream.readByte();
                    StringObject classname = readNewString(stc, stream);
                    fields[s] = new field(FieldType.get(fieldType), fieldName, classname);
                } else {
                    throw new IOException("invalid field type char: " + hex(fieldType));
                }
            }
            classdesc cd = new classdesc(classdesctype.NORMALCLASS);
            cd.name = name;
            cd.serialVersionUID = serialVersionUID;
            cd.handle = handle;
            cd.descflags = descflags;
            cd.fields = fields;
            cd.annotations = read_classAnnotation(stream);
            cd.superclass = readClassDesc(stream);
            setHandle(handle, cd);
            debug("read new classdesc: handle " + hex(handle) + " name " + name);
            return cd;
        } else if (tc == ObjectStreamConstants.TC_NULL) {
            if (mustBeNew) {
                throw new ValidityException("expected new class description -- got null!");
            }
            debug("read null classdesc");
            return null;
        } else if (tc == ObjectStreamConstants.TC_REFERENCE) {
            if (mustBeNew) {
                throw new ValidityException("expected new class description -- got a reference!");
            }
            IContent c = readPrevObject(stream);
            if (!(c instanceof classdesc)) {
                throw new IOException("referenced object not a class description!");
            }
            classdesc cd = (classdesc) c;
            return cd;
        } else if (tc == ObjectStreamConstants.TC_PROXYCLASSDESC) {
            int handle = newHandle();
            int interfaceCount = stream.readInt();
            if (interfaceCount < 0) {
                throw new IOException("invalid proxy interface count: " + hex(interfaceCount));
            }
            String[] interfaces = new String[interfaceCount];
            for (int i = 0; i < interfaceCount; i++) {
                interfaces[i] = stream.readUTF();
            }
            classdesc cd = new classdesc(classdesctype.PROXYCLASS);
            cd.handle = handle;
            cd.interfaces = interfaces;
            cd.annotations = read_classAnnotation(stream);
            cd.superclass = readClassDesc(stream);
            setHandle(handle, cd);
            cd.name = "(proxy class; no name)";
            debug("read new proxy classdesc: handle " + hex(handle) + " names [" + Arrays.toString(interfaces) + "]");
            return cd;
        } else {
            throw new ValidityException("expected a valid class description starter got " + hex(tc));
        }
    }

    public arrayobj readNewArray(DataInputStream stream) throws IOException {
        classdesc cd = readClassDesc(stream);
        int handle = newHandle();
        debug("reading new array: handle " + hex(handle) + " classdesc " + cd.toString());
        if (cd.name.length() < 2) {
            throw new IOException("invalid name in array classdesc: " + cd.name);
        }
        ObjectList ac = readArrayValues(cd.name.substring(1), stream);
        return new arrayobj(handle, cd, ac);
    }

    public ObjectList readArrayValues(String string, DataInputStream stream) throws IOException {
        byte firstByte = string.getBytes(StandardCharsets.UTF_8)[0];
        FieldType type = FieldType.get(firstByte);
        int size = stream.readInt();
        if (size < 0) {
            throw new IOException("invalid array size: " + size);
        }

        ObjectList objects = new ObjectList(type);
        for (int i = 0; i < size; i++) {
            objects.add(readFieldValue(type, stream));
        }
        return objects;
    }

    public ClassObject readNewClass(DataInputStream stream) throws IOException {
        classdesc cd = readClassDesc(stream);
        int handle = newHandle();
        debug("reading new class: handle " + hex(handle) + " classdesc " + cd.toString());
        ClassObject clazz = new ClassObject(handle, cd);
        setHandle(handle, clazz);
        return clazz;
    }

    public EnumObject readNewEnum(DataInputStream stream) throws IOException {
        classdesc cd = readClassDesc(stream);
        if (cd == null) {
            throw new IOException("enum classdesc can't be null!");
        }
        int handle = newHandle();
        debug("reading new enum: handle " + hex(handle) + " classdesc " + cd);
        byte tc = stream.readByte();
        StringObject stringObject = readNewString(tc, stream);
        cd.addEnum(stringObject.value);
        setHandle(handle, stringObject);
        return new EnumObject(handle, cd, stringObject);
    }

    public StringObject readNewString(byte tc, DataInputStream stream) throws IOException {
        byte[] data;
        if (tc == ObjectStreamConstants.TC_REFERENCE) {
            IContent content = readPrevObject(stream);
            if (!(content instanceof StringObject)) {
                throw new IOException("got reference for a string, but referenced value was something else!");
            }
            return (StringObject) content;
        }
        int handle = newHandle();
        if (tc == ObjectStreamConstants.TC_STRING) {
            int length = stream.readUnsignedShort();
            data = new byte[length];
        } else if (tc == ObjectStreamConstants.TC_LONGSTRING) {
            long length = stream.readLong();
            if (length < 0) {
                throw new IOException("invalid long string length: " + length);
            }
            if (length > 2147483647) {
                throw new IOException("long string is too long: " + length);
            }
            if (length < 65536) {
                debugerr("warning: small string length encoded as TC_LONGSTRING: " + length);
            }
            data = new byte[(int) length];
        } else if (tc == ObjectStreamConstants.TC_NULL) {
            throw new ValidityException("stream signaled TC_NULL when string type expected!");
        } else {
            throw new IOException("invalid tc byte in string: " + hex(tc));
        }
        stream.readFully(data);
        debug("reading new string: handle " + hex(handle) + " bufsz " + data.length);
        StringObject sobj = new StringObject(handle, data);
        setHandle(handle, sobj);
        return sobj;
    }

    public BlockData readBlockdata(byte tc, DataInputStream stream) throws IOException {
        int size;
        if (tc == ObjectStreamConstants.TC_BLOCKDATA) {
            size = stream.readUnsignedByte();
        } else if (tc == ObjectStreamConstants.TC_BLOCKDATALONG) {
            size = stream.readInt();
        } else {
            throw new IOException("invalid tc value for blockdata: " + hex(tc));
        }
        if (size < 0) {
            throw new IOException("invalid value for blockdata size: " + size);
        }
        byte[] data = new byte[size];
        stream.readFully(data);
        debug("read blockdata of size " + size);
        return new BlockData(data);
    }

    public Instance readNewObject(DataInputStream stream) throws IOException {
        classdesc cd = readClassDesc(stream);
        int handle = newHandle();
        debug("reading new object: handle " + hex(handle) + " classdesc " + cd.toString());
        Instance instance = new Instance();
        instance.classdesc = cd;
        instance.handle = handle;
        setHandle(handle, instance);
        readClassData(stream, instance);
        debug("done reading object for handle " + hex(handle));
        return instance;
    }

    /**
     * <p>
     * Read the next object corresponding to the spec grammar rule "content", and return
     * an object of type content.
     * </p>
     *
     * <p>
     * Usually, there is a 1:1 mapping of content items and returned instances.  The
     * one case where this isn't true is when an exception is embedded inside another
     * object.  When this is encountered, only the serialized exception object is
     * returned; it's up to the caller to backtrack in order to gather any data from the
     * object that was being serialized when the exception was thrown.
     * </p>
     *
     * @param tc the last byte read from the stream; it must be one of the TC_* values
     * within ObjectStreamConstants.*
     * @param stream the DataInputStream to read from
     * @param isBlockData whether or not to read TC_BLOCKDATA (this is the difference
     * between spec rules "object" and "content").
     * @return an object representing the last read item from the stream 
     * @throws IOException when a validity or I/O error occurs while reading
     */
    public IContent readContent(byte tc, DataInputStream stream, boolean isBlockData) throws IOException {
        try {
            return switch (tc) {
                case ObjectStreamConstants.TC_OBJECT -> readNewObject(stream);
                case ObjectStreamConstants.TC_CLASS -> readNewClass(stream);
                case ObjectStreamConstants.TC_ARRAY -> readNewArray(stream);
                case ObjectStreamConstants.TC_STRING, ObjectStreamConstants.TC_LONGSTRING -> readNewString(tc, stream);
                case ObjectStreamConstants.TC_ENUM -> readNewEnum(stream);
                case ObjectStreamConstants.TC_CLASSDESC, ObjectStreamConstants.TC_PROXYCLASSDESC ->
                        handleNewClassDesc(tc, stream);
                case ObjectStreamConstants.TC_REFERENCE -> readPrevObject(stream);
                case ObjectStreamConstants.TC_NULL -> null;
                case ObjectStreamConstants.TC_EXCEPTION -> readException(stream);
                case ObjectStreamConstants.TC_BLOCKDATA, ObjectStreamConstants.TC_BLOCKDATALONG -> {
                    if (!isBlockData) {
                        throw new IOException("got a isBlockData TC_*, but not allowed here: " + hex(tc));
                    }
                    yield readBlockdata(tc, stream);
                }
                default -> throw new IOException("unknown content tc byte in stream: " + hex(tc));
            };
        } catch (ExceptionReadException ere) {
            return ere.getExceptionObject();
        }
    }

    /**
     * <p>
     * Reads in an entire ObjectOutputStream output on the given stream, filing 
     * this object's content and handle maps with data about the objects in the stream.  
     * </p>
     *
     * <p>
     * If shouldConnect inputStream true, then jdeserialize will attempt to identify member classes
     * by their names according to the details laid out in the Inner Classes
     * Specification.  If it finds one, it will set the classdesc's flag indicating that
     * it inputStream an member class, and it will create a reference in its enclosing class.
     * </p>
     *
     * @param inputStream an open InputStream on a serialized stream of data
     * @param shouldConnect true if jdeserialize should attempt to identify and connect
     * member classes with their enclosing classes
     *
     * Also see the <pre>connectMemberClasses</pre> method for more information on the 
     * member-class-detection algorithm.
     */
    public void run(InputStream inputStream, boolean shouldConnect) throws IOException {
        try (LoggerInputStream loggerStream = new LoggerInputStream(inputStream); DataInputStream stream = new DataInputStream(loggerStream)) {
            short magic = stream.readShort();
            if (magic != ObjectStreamConstants.STREAM_MAGIC) {
                throw new ValidityException("file magic mismatch!  expected " + ObjectStreamConstants.STREAM_MAGIC + ", got " + magic);
            }
            short streamVersion = stream.readShort();
            if (streamVersion != ObjectStreamConstants.STREAM_VERSION) {
                throw new ValidityException("file version mismatch!  expected " + ObjectStreamConstants.STREAM_VERSION + ", got " + streamVersion);
            }
            reset();
            IContent = new ArrayList<>();
            while (true) {
                byte tc;
                try {
                    loggerStream.record();
                    tc = stream.readByte();
                    if (tc == ObjectStreamConstants.TC_RESET) {
                        reset();
                        continue;
                    }
                } catch (EOFException ignored) {
                    break;
                }
                IContent content = readContent(tc, stream, true);
                System.out.println("read: " + content.toString());
                if (content.isExceptionObject()) {
                    content = new exceptionstate(content, loggerStream.getRecordedData());
                }
                IContent.add(content);
            }
        }
        for (IContent c : handles.values()) {
            c.validate();
        }
        if (shouldConnect) {
            connectMemberClasses();
            for (IContent c : handles.values()) {
                c.validate();
            }
        }
        if (!handles.isEmpty()) {
            handleMaps.add(new HashMap<>(handles));
        }
    }

    public void dump(OptionManager go) throws IOException {
        if (go.hasOption("-blockdata") || go.hasOption("-blockdatamanifest")) {
            List<String> blockData = go.getArguments("-blockdata");
            List<String> blockDataManifest = go.getArguments("-blockdatamanifest");
            FileOutputStream outputStream = null, mos = null;
            PrintWriter writer = null;
            try {
                if (blockData != null && !blockData.isEmpty()) {
                    outputStream = new FileOutputStream(blockData.getFirst());
                }
                if (blockDataManifest != null && !Objects.requireNonNull(blockData).isEmpty()) {
                    mos = new FileOutputStream(blockDataManifest.getFirst());
                    writer = new PrintWriter(mos);
                    writer.println("# Each line in this file that doesn't begin with a '#' contains the size of");
                    writer.println("# an individual blockdata block written to the stream.");
                }
                for (IContent content : IContent) {
                    System.out.println(content.toString());
                    if (content instanceof BlockData bd) {
                        if (mos != null) {
                            writer.println(bd.buf.length);
                        }
                        if (outputStream != null) {
                            outputStream.write(bd.buf);
                        }
                    }
                }
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ignore) {
                    }
                }
                if (mos != null) {
                    try {
                        Objects.requireNonNull(writer).close();
                        mos.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
        if (!go.hasOption("-nocontent")) {
            System.out.println("//// BEGIN stream content output");
            for (IContent c : IContent) {
                System.out.println(c.toString());
            }
            System.out.println("//// END stream content output");
            System.out.println();
        }

        if (!go.hasOption("-noclasses")) {
            boolean showArray = go.hasOption("-showarrays");
            List<String> filter = go.getArguments("-filter");
            System.out.println("//// BEGIN class declarations"
                    + (showArray ? "" : " (excluding array classes)")
                    + ((filter != null && !filter.isEmpty())
                    ? " (exclusion filter " + filter.getFirst() + ")"
                    : ""));
            for (IContent c : handles.values()) {
                if (c instanceof classdesc cl) {
                    if (!showArray && cl.isArrayClass()) {
                        continue;
                    }
                    // Member classes will be streamplayed as part of their enclosing
                    // classes.
                    if (cl.isStaticMemberClass() || cl.isInnerClass()) {
                        continue;
                    }
                    if (filter != null && !filter.isEmpty() && cl.name.matches(filter.getFirst())) {
                        continue;
                    }
                    dumpClassDesc(0, cl, System.out, go.hasOption("-fixnames"));
                    System.out.println();
                }
            }
            System.out.println("//// END class declarations");
            System.out.println();
        }
        if (!go.hasOption("-noinstances")) {
            System.out.println("//// BEGIN instance dump");
            for (IContent c : handles.values()) {
                if (c instanceof Instance instance) {
                    dump_Instance(instance, System.out);
                }
            }
            System.out.println("//// END instance dump");
            System.out.println();
        }
    }


    /**
     * <p>
     * Connects member classes according to the rules specified by the JDK 1.1 Inner
     * Classes Specification.  
     * </p>
     *
     * <pre>
     * Inner classes:
     * for each class C containing an object reference member R named this$N, do:
     *     if the name of C matches the pattern O$I
     *     AND the name O matches the name of an existing type T
     *     AND T is the exact type referred to by R, then:
     *         don't streamplay the declaration of R in normal dumping,
     *         consider C to be an inner class of O named I
     *
     * Static member classes (after):
     * for each class C matching the pattern O$I, 
     * where O is the name of a class in the same package
     * AND C is not an inner class according to the above algorithm:
     *     consider C to be an inner class of O named I
     * </pre>
     *
     * <p>
     * This functions fills in the isInnerClass value in classdesc, the
     * isInnerClassReference value in field, the isLocalInnerClass value in 
     * classdesc, and the isStaticMemberClass value in classdesc where necessary.
     * </p>
     *
     * <p>
     * A word on static classes: serializing a static member class S doesn't inherently
     * require serialization of its parent class P.  Unlike inner classes, S doesn't
     * retain an instance of P, and therefore P's class description doesn't need to be
     * written.  In these cases, if parent classes can be found, their static member
     * classes will be connected; but if they can't be found, the names will not be
     * changed and no ValidityException will be thrown.
     * </p>
     *
     * @throws ValidityException if the found values don't correspond to spec
     */
    public void connectMemberClasses() throws IOException {
        HashMap<classdesc, String> newNames = new HashMap<>();
        HashMap<String, classdesc> classes = new HashMap<>();
        HashSet<String> classnames = new HashSet<>();
        for (IContent c : handles.values()) {
            if (!(c instanceof classdesc)) {
                continue;
            }
            classdesc cd = (classdesc) c;
            classes.put(cd.name, cd);
            classnames.add(cd.name);
        }
        Pattern fpat = Pattern.compile("^this\\$(\\d+)$");
        Pattern clpat = Pattern.compile("^((?:[^\\$]+\\$)*[^\\$]+)\\$([^\\$]+)$");
        for (classdesc cd : classes.values()) {
            if (cd.classtype == classdesctype.PROXYCLASS) {
                continue;
            }
            for (field f : cd.fields) {
                if (f.type != FieldType.OBJECT) {
                    continue;
                }
                Matcher m = fpat.matcher(f.name);
                if (!m.matches()) {
                    continue;
                }
                boolean islocal = false;
                Matcher clmat = clpat.matcher(cd.name);
                if (!clmat.matches()) {
                    throw new ValidityException("inner class enclosing-class reference field exists, but class name doesn't match expected pattern: class " + cd.name + " field " + f.name);
                }
                String outer = clmat.group(1), inner = clmat.group(2);
                classdesc outercd = classes.get(outer);
                if (outercd == null) {
                    throw new ValidityException("couldn't connect inner classes: outer class not found for field name " + f.name);
                }
                if (!outercd.name.equals(f.getJavaType())) {
                    throw new ValidityException("outer class field type doesn't match field type name: " + f.classname.value + " outer class name " + outercd.name);
                }
                outercd.addInnerClass(cd);
                cd.setIsLocalInnerClass(islocal);
                cd.setIsInnerClass(true);
                f.setIsInnerClassReference(true);
                newNames.put(cd, inner);
            }
        }
        for (classdesc cd : classes.values()) {
            if (cd.classtype == classdesctype.PROXYCLASS) {
                continue;
            }
            if (cd.isInnerClass()) {
                continue;
            }
            Matcher clmat = clpat.matcher(cd.name);
            if (!clmat.matches()) {
                continue;
            }
            String outer = clmat.group(1), inner = clmat.group(2);
            classdesc outercd = classes.get(outer);
            if (outercd != null) {
                outercd.addInnerClass(cd);
                cd.setIsStaticMemberClass(true);
                newNames.put(cd, inner);
            }
        }
        for (classdesc ncd : newNames.keySet()) {
            String newname = newNames.get(ncd);
            if (classnames.contains(newname)) {
                throw new ValidityException("can't rename class from " + ncd.name + " to " + newname + " -- class already exists!");
            }
            for (classdesc cd : classes.values()) {
                if (cd.classtype == classdesctype.PROXYCLASS) {
                    continue;
                }
                for (field f : cd.fields) {
                    if (f.getJavaType().equals(ncd.name)) {
                        f.setReferenceTypeName(newname);
                    }
                }
            }
            if (classnames.remove(ncd.name) == false) {
                throw new ValidityException("tried to remove " + ncd.name + " from classnames cache, but couldn't find it!");
            }
            ncd.name = newname;
            if (classnames.add(newname) == false) {
                throw new ValidityException("can't rename class to " + newname + " -- class already exists!");
            }
        }
    }

    /**
     * Decodes a class name according to the field-descriptor format in the jvm spec,
     * section 4.3.2.
     * @param fdesc name in field-descriptor format (Lfoo/bar/baz;)
     * @param convertSlashes true iff slashes should be replaced with periods (true for
     * "real" field-descriptor format; false for names in classdesc)
     * @return a fully-qualified class name
     * @throws ValidityException if the name isn't valid
     */
    public static String decodeClassName(String fdesc, boolean convertSlashes) throws ValidityException {
        if (fdesc.charAt(0) != 'L' || fdesc.charAt(fdesc.length() - 1) != ';' || fdesc.length() < 3) {
            throw new ValidityException("invalid name (not in field-descriptor format): " + fdesc);
        }
        String subs = fdesc.substring(1, fdesc.length() - 1);
        if (convertSlashes) {
            return subs.replace('/', '.');
        }
        return subs;
    }

    public static String hexnoprefix(long value) {
        return hexnoprefix(value, 2);
    }

    public static String hexnoprefix(long value, int length) {
        if (value < 0) {
            value = 256 + value;
        }
        String s = Long.toString(value, 16);
        while (s.length() < length) {
            s = "0" + s;
        }
        return s;
    }

    public static String hex(long value) {
        return "0x" + hexnoprefix(value);
    }

    public static void debugerr(String message) {
        System.err.println(message);
    }

    public void debug(String message) {
        if (debugEnabled) {
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        HashMap<String, Integer> options = new HashMap<>();
        OptionManager go = new OptionManager();
        go.addOption("-help", 0, "Show this list.");
        go.addOption("-debug", 0, "Write debug info generated during parsing to stdout.");
        go.addOption("-filter", 1, "Exclude classes that match the given String.matches() regex from class output.");
        go.addOption("-nocontent", 0, "Don't output descriptions of the content in the stream.");
        go.addOption("-noinstances", 0, "Don't output descriptions of every instance.");
        go.addOption("-showarrays", 0, "Show array class declarations (e.g. int[]).");
        go.addOption("-noconnect", 0, "Don't attempt to connect member classes to their enclosing classes.");
        go.addOption("-fixnames", 0, "In class names, replace illegal Java identifier characters with legal ones.");
        go.addOption("-noclasses", 0, "Don't output class declarations.");
        go.addOption("-blockdata", 1, "Write raw blockdata out to the specified file.");
        go.addOption("-blockdatamanifest", 1, "Write blockdata manifest out to the specified file.");
        try {
            go.parse(args);
        } catch (OptionManager.OptionParseException ope) {
            System.err.println("argument error: " + ope.getMessage());
            System.out.println(go.getDescriptionString());
            System.exit(1);
        }
        if (go.hasOption("-help")) {
            System.out.println(go.getDescriptionString());
            System.exit(1);
        }
        List<String> fargs = go.getFileArguments();
        if (fargs.isEmpty()) {
            debugerr("args: [options] file1 [file2 .. fileN]");
            System.err.println();
            System.err.println(go.getDescriptionString());
            System.exit(1);
        }
        for (String filename : fargs) {
            try (FileInputStream fis = new FileInputStream(filename)) {
                //TODO: figure out: JDeserialize jd = new JDeserialize(filename);
                JDeserialize jd = new JDeserialize();
                jd.debugEnabled = go.hasOption("-debug");
                jd.run(fis, !go.hasOption("-noconnect"));
                jd.dump(go);
            } catch (EOFException eoe) {
                debugerr("EOF error while attempting to decode file " + filename + ": " + eoe.getMessage());
                eoe.printStackTrace();
            } catch (IOException ioe) {
                debugerr("error while attempting to decode file " + filename + ": " + ioe.getMessage());
                ioe.printStackTrace();
            }
        }
    }
}
