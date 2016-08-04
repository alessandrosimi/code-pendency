package code.pendency;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ClassFileParser {

    public static final int JAVA_MAGIC = 0xCAFEBABE;
    public static final char CLASS_DESCRIPTOR = 'L';
    public static final int ACC_INTERFACE = 0x200;
    public static final int ACC_ABSTRACT = 0x400;

    private final Filter filter;
    private final List<ParserListener> listeners;

    ClassFileParser(Filter filter, List<ParserListener> listeners) {
        this.filter = filter;
        this.listeners = listeners;
    }

    JavaClass parse(InputStream is) throws IOException {
        return parse(is, "<sourcecode>");
    }

    JavaClass parse(InputStream is, String fileName) throws IOException {
        DataInputStream in = new DataInputStream(is);
        if (isJavaMagic(in)) {
            int minorVersion = parseMinorVersion(in);
            int majorVersion = parseMajorVersion(in);
            // Constant pool
            ClassFileConstantsPool constantsPool = parseConstantPool(in);
            // Access flag
            int accessFlags = readAccessFlags(in);
            boolean isAbstract = isAbstract(accessFlags);
            boolean isInterface = isInterface(accessFlags);
            // Class name
            String className = parseClassName(in, constantsPool);
            // Super class
            List<String> importedClasses = new ArrayList<String>();
            String superClassName = parseSuperClassName(in, constantsPool);
            importedClasses.add(superClassName);
            // Interfaces
            List<String> interfaceNames = parseInterfaces(in, constantsPool);
            importedClasses.addAll(interfaceNames);
            // Fields
            ClassFileFieldOrMethodInfo[] fields = parseFields(in, constantsPool);
            List<String> fieldTypes = extractTypesFromFieldOrMethod(fields);
            importedClasses.addAll(fieldTypes);
            // Methods
            ClassFileFieldOrMethodInfo[] methods = parseMethods(in, constantsPool);
            List<String> methodTypes = extractTypesFromFieldOrMethod(methods);
            importedClasses.addAll(methodTypes);
            // Source file
            ClassFileAttributeInfo[] attributes = parseAttributes(in, constantsPool);
            String sourceFile = extractSourceFile(attributes, constantsPool);
            // Constant references
            List<String> constantReferences = extractClassConstantReferences(constantsPool);
            importedClasses.addAll(constantReferences);
            // Annotations references
            List<ClassFileAnnotationInfo.ClassFileAnnotationValues> annotations = extractAnnotationInfo(attributes, fields, methods);
            List<String> annotationTypes = extractTypesFromAnnotations(constantsPool, annotations);
            importedClasses.addAll(annotationTypes);
            // Add import
            importedClasses = filterImport(importedClasses);
            // Fire listeners
            JavaClass javaClass = new JavaClass(
                minorVersion,
                majorVersion,
                className,
                sourceFile,
                fileName,
                isAbstract,
                isInterface,
                importedClasses
            );
            onParsedJavaClass(javaClass);
            return javaClass;
        } else {
            throw new IOException("Invalid java class file: does no begin with the java magic code");
        }
    }

    private List<String> filterImport(List<String> imports) {
        List<String> filtered = new ArrayList<String>(imports.size());
        for (String importClass : imports) {
            String className = removeEndingSemiColon(slashesToDots(importClass));
            if (filter.accept(className)) {
                filtered.add(className);
            }
        }
        return filtered;
    }

    private boolean isJavaMagic(DataInputStream in) throws IOException {
        int magic = in.readInt();
        return magic == JAVA_MAGIC;
    }

    private int parseMinorVersion(DataInputStream in) throws IOException {
        return in.readUnsignedShort();
    }

    private int parseMajorVersion(DataInputStream in) throws IOException {
        return in.readUnsignedShort();
    }

    private ClassFileConstantsPool parseConstantPool(DataInputStream in) throws IOException {
        int constantPoolSize = in.readUnsignedShort();
        ClassFileConstantsPool.Constant[] pool = new ClassFileConstantsPool.Constant[constantPoolSize];
        for (int i = 1; i < constantPoolSize; i++) {
            ClassFileConstantsPool.Constant constant = ClassFileConstantsPool.parseNextConstant(in);
            pool[i] = constant;
            if (constant.isEightByte()) {
                i++;
            }
        }
        return new ClassFileConstantsPool(pool);
    }

    private int readAccessFlags(DataInputStream in) throws IOException {
        return in.readUnsignedShort();
    }

    private boolean isAbstract(int accessFlags) {
        boolean isAbstract = ((accessFlags & ACC_ABSTRACT) != 0);
        boolean isInterface = ((accessFlags & ACC_INTERFACE) != 0);
        return isAbstract && !isInterface;
    }

    private boolean isInterface(int accessFlags) {
        return ((accessFlags & ACC_INTERFACE) != 0);
    }

    private String parseClassName(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        int entryIndex = in.readUnsignedShort();
        return getClassConstantName(entryIndex, constantsPool);
    }

    private String parseSuperClassName(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        int entryIndex = in.readUnsignedShort();
        return getClassConstantName(entryIndex, constantsPool);
    }

    private List<String> parseInterfaces(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        int interfacesCount = in.readUnsignedShort();
        List<String> interfaceNames = new ArrayList<String>(interfacesCount);
        for (int i = 0; i < interfacesCount; i++) {
            int entryIndex = in.readUnsignedShort();
            interfaceNames.add(getClassConstantName(entryIndex, constantsPool));
        }
        return interfaceNames;
    }

    private ClassFileFieldOrMethodInfo[] parseFields(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        int fieldsCount = in.readUnsignedShort();
        ClassFileFieldOrMethodInfo[] fields = new ClassFileFieldOrMethodInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            fields[i] = new ClassFileFieldOrMethodInfo(in, constantsPool);
        }
        return fields;
    }

    private ClassFileFieldOrMethodInfo[] parseMethods(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        int methodsCount = in.readUnsignedShort();
        ClassFileFieldOrMethodInfo[] methods = new ClassFileFieldOrMethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            methods[i] = new ClassFileFieldOrMethodInfo(in, constantsPool);
        }
        return methods;
    }

    private List<String> extractTypesFromFieldOrMethod(ClassFileFieldOrMethodInfo[] fieldOrMethods) {
        List<String> types = new ArrayList<String>();
        for (ClassFileFieldOrMethodInfo fieldOrMethod : fieldOrMethods) {
            types.addAll(Arrays.asList(fieldOrMethod.types));
        }
        return types;
    }

    private ClassFileAttributeInfo[] parseAttributes(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        int attributesCount = in.readUnsignedShort();
        ClassFileAttributeInfo[] attributes = new ClassFileAttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            attributes[i] = new ClassFileAttributeInfo(in, constantsPool);
        }
        return attributes;
    }

    private String extractSourceFile(ClassFileAttributeInfo[] attributes, ClassFileConstantsPool constantsPool) throws IOException {
        for (ClassFileAttributeInfo attribute : attributes) {
            // Section 4.7.7 of VM Spec - Class File Format
            if ("SourceFile".equals(attribute.name)) {
                byte[] b = attribute.value;
                int b0 = b[0] < 0 ? b[0] + 256 : b[0];
                int b1 = b[1] < 0 ? b[1] + 256 : b[1];
                int pe = b0 * 256 + b1;
                return constantsPool.getUTF8Entry(pe);
            }
        }
        return null;
    }

    private List<String> extractClassConstantReferences(ClassFileConstantsPool constantsPool) throws IOException {
        List<String> constantReferences = new ArrayList<String>(constantsPool.getEntries().size());
        for (ClassFileConstantsPool.Constant constant : constantsPool.getEntries()) {
            if (constant.tag == ClassFileConstantsPool.CONSTANT_CLASS) {
                String name = constantsPool.getUTF8Entry(constant.nameIndex);
                constantReferences.add(name);
            }
        }
        return constantReferences;
    }

    private List<ClassFileAnnotationInfo.ClassFileAnnotationValues> extractAnnotationInfo(ClassFileAttributeInfo[] attributes,
                                                                                          ClassFileFieldOrMethodInfo[] fields,
                                                                                          ClassFileFieldOrMethodInfo[] methods) throws IOException {
        List<ClassFileAnnotationInfo.ClassFileAnnotationValues> annotations = new ArrayList<ClassFileAnnotationInfo.ClassFileAnnotationValues>();
        for (ClassFileAttributeInfo attribute : attributes) {
            annotations.addAll(ClassFileAnnotationInfo.extract(attribute));
        }
        for (ClassFileFieldOrMethodInfo field : fields) {
            annotations.addAll(ClassFileAnnotationInfo.extract(field.runtimeVisibleAnnotations));
        }
        for (ClassFileFieldOrMethodInfo method : methods) {
            annotations.addAll(ClassFileAnnotationInfo.extract(method.runtimeVisibleAnnotations));
        }
        return annotations;
    }

    private List<String> extractTypesFromAnnotations(ClassFileConstantsPool constantsPool,
                                                     List<ClassFileAnnotationInfo.ClassFileAnnotationValues> annotations) throws IOException {
        List<String> types = new ArrayList<String>();
        for (ClassFileAnnotationInfo.ClassFileAnnotationValues annotationInfo : annotations) {
            collectType(annotationInfo, types, constantsPool);
        }
        return types;
    }

    private void collectType(ClassFileAnnotationInfo.ClassFileAnnotationValues annotation, List<String> types, ClassFileConstantsPool constants) throws IOException {
        types.add(constants.getUTF8Entry(annotation.typeIndex).substring(1));
        for (ClassFileAnnotationInfo value : annotation.values) {
            types.add(constants.getUTF8Entry(value.typeIndex).substring(1));
            if (value instanceof ClassFileAnnotationInfo.ClassFileAnnotationValues) {
                collectType((ClassFileAnnotationInfo.ClassFileAnnotationValues) value, types, constants);
            }
        }
    }

    private String getClassConstantName(int entryIndex, ClassFileConstantsPool constantsPool) throws IOException {
        ClassFileConstantsPool.Constant entry = constantsPool.getEntry(entryIndex);
        if (entry == null) {
            return "";
        }
        return slashesToDots(constantsPool.getUTF8Entry(entry.nameIndex));
    }

    private String removeEndingSemiColon(String className) {
        return className.endsWith(";") ? className.substring(0, className.length() - 1) : className;
    }

    private String slashesToDots(String s) {
        return s.replace('/', '.');
    }

    protected void onParsedJavaClass(JavaClass jClass) {
        for (Object parseListener : listeners) {
            ((ParserListener) parseListener).onParsedJavaClass(jClass);
        }
    }

}
