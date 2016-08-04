package code.pendency;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ClassFileFieldOrMethodInfo {
    final int accessFlags;
    final int nameIndex;
    final int descriptorIndex;
    final String[] types;
    final ClassFileAttributeInfo runtimeVisibleAnnotations;

    ClassFileFieldOrMethodInfo(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        int accessFlags = in.readUnsignedShort();
        int nameIndex = in.readUnsignedShort();
        int descriptionIndex = in.readUnsignedShort();
        this.accessFlags = accessFlags;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptionIndex;
        String descriptor = constantsPool.getUTF8Entry(descriptionIndex);
        this.types = descriptorToTypes(descriptor);
        int attributesCount = in.readUnsignedShort();
        ClassFileAttributeInfo runtimeVisibleAnnotation = null;
        for (int a = 0; a < attributesCount; a++) {
            ClassFileAttributeInfo attribute = new ClassFileAttributeInfo(in, constantsPool);
            if ("RuntimeVisibleAnnotations".equals(attribute.name)) {
                runtimeVisibleAnnotation = attribute;
            }
        }
        this.runtimeVisibleAnnotations = runtimeVisibleAnnotation;
    }

    // The format is (T*)T where T can be L<name-of-the-class>; or B, C, D, F, I, J, S, Z for primitives.
    private String[] descriptorToTypes(String descriptor) {
        String[] items = descriptor.split(";");
        List<String> types = new ArrayList<String>(items.length);
        for (String item : items) {
            int index = item.indexOf(ClassFileParser.CLASS_DESCRIPTOR);
            if (index != -1) {
                types.add(item.substring(index + 1));
            }
        }
        return types.toArray(new String[types.size()]);
    }

}
