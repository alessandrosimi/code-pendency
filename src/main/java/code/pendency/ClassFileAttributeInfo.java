package code.pendency;

import java.io.DataInputStream;
import java.io.IOException;

class ClassFileAttributeInfo {

    final String name;
    final byte[] value;

    ClassFileAttributeInfo(DataInputStream in, ClassFileConstantsPool constantsPool) throws IOException {
        String name = null;
        int nameIndex = in.readUnsignedShort();
        if (nameIndex != -1) {
            name = constantsPool.getUTF8Entry(nameIndex);
        }
        this.name = name;
        int attributeLength = in.readInt();
        byte[] value = new byte[attributeLength];
        for (int b = 0; b < attributeLength; b++) {
            value[b] = in.readByte();
        }
        this.value = value;
    }

}
