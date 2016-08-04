package code.pendency;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ClassFileConstantsPool {

    public static final int CONSTANT_UTF8 = 1;
    public static final int CONSTANT_UNICODE = 2;
    public static final int CONSTANT_INTEGER = 3;
    public static final int CONSTANT_FLOAT = 4;
    public static final int CONSTANT_LONG = 5;
    public static final int CONSTANT_DOUBLE = 6;
    public static final int CONSTANT_CLASS = 7;
    public static final int CONSTANT_STRING = 8;
    public static final int CONSTANT_FIELD = 9;
    public static final int CONSTANT_METHOD = 10;
    public static final int CONSTANT_INTERFACEMETHOD = 11;
    public static final int CONSTANT_NAMEANDTYPE = 12;
    public static final int CONSTANT_METHOD_HANDLE = 15;
    public static final int CONSTANT_METHOD_TYPE = 16;
    public static final int CONSTANT_INVOKEDYNAMIC = 18;

    private final Constant[] pool;

    ClassFileConstantsPool(Constant[] pool) {
        this.pool = pool;
    }

    List<Constant> getEntries() {
        List<Constant> constants = new ArrayList<Constant>();
        for (int i = 1; i < pool.length; i++) {
            Constant constant = pool[i];
            constants.add(constant);
            if (constant.tag == CONSTANT_DOUBLE || constant.tag == CONSTANT_LONG) {
                i++;
            }
        }
        return constants;
    }

    Constant getEntry(int entryIndex) throws IOException {
        if (entryIndex < 0 || entryIndex >= pool.length) {
            throw new IOException("Illegal constant pool index : " + entryIndex);
        }
        return pool[entryIndex];
    }

    String getUTF8Entry(int entryIndex) throws IOException {
        Constant entry = getEntry(entryIndex);
        if (entry.tag == CONSTANT_UTF8) {
            return (String) entry.value;
        } else {
            throw new IOException("Constant pool entry is not a UTF8 type: " + entryIndex);
        }
    }

    static class Constant<T> {
        final byte tag;
        final int nameIndex;
        final int typeIndex;
        final T value;

        private Constant(byte tag, int nameIndex, int typeIndex, T value) {
            this.tag = tag;
            this.nameIndex = nameIndex;
            this.typeIndex = typeIndex;
            this.value = value;
        }

        boolean isEightByte() {
            return tag == CONSTANT_DOUBLE || tag == CONSTANT_LONG;
        }

        static Constant withNameIndex(byte tag, int nameIndex) {
            return new Constant<Object>(tag, nameIndex, -1, null);
        }

        static Constant withNameAndTypeIndex(byte tag, int nameIndex, int typeIndex) {
            return new Constant<Object>(tag, nameIndex, typeIndex, null);
        }

        static <T> Constant<T> withValue(byte tag, T value) {
            return new Constant<T>(tag, -1, -1, value);
        }

    }

    static ClassFileConstantsPool.Constant parseNextConstant(DataInputStream in) throws IOException {
        ClassFileConstantsPool.Constant result;
        byte tag = in.readByte();
        switch (tag) {
            case (CONSTANT_CLASS):
            case (CONSTANT_STRING):
            case (CONSTANT_METHOD_TYPE):
                result = ClassFileConstantsPool.Constant.withNameIndex(tag, in.readUnsignedShort());
                break;
            case (CONSTANT_FIELD):
            case (CONSTANT_METHOD):
            case (CONSTANT_INTERFACEMETHOD):
            case (CONSTANT_NAMEANDTYPE):
            case (CONSTANT_INVOKEDYNAMIC):
                result = ClassFileConstantsPool.Constant.withNameAndTypeIndex(tag, in.readUnsignedShort(), in.readUnsignedShort());
                break;
            case (CONSTANT_INTEGER):
                result = ClassFileConstantsPool.Constant.withValue(tag, in.readInt());
                break;
            case (CONSTANT_FLOAT):
                result = ClassFileConstantsPool.Constant.withValue(tag, in.readFloat());
                break;
            case (CONSTANT_LONG):
                result = ClassFileConstantsPool.Constant.withValue(tag, in.readLong());
                break;
            case (CONSTANT_DOUBLE):
                result = ClassFileConstantsPool.Constant.withValue(tag, in.readDouble());
                break;
            case (CONSTANT_UTF8):
                result = ClassFileConstantsPool.Constant.withValue(tag, in.readUTF());
                break;
            case (CONSTANT_METHOD_HANDLE):
                result = ClassFileConstantsPool.Constant.withNameAndTypeIndex(tag, in.readByte(), in.readUnsignedShort());
                break;
            default:
                throw new IOException("Unknown constant: " + tag);
        }
        return result;
    }

}
