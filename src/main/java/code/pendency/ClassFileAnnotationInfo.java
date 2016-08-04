package code.pendency;

import java.util.ArrayList;
import java.util.List;

class ClassFileAnnotationInfo {
    final int typeIndex;

    private ClassFileAnnotationInfo(AnnotationData data) {
        this.typeIndex = data.nextInt();
    }

    static List<ClassFileAnnotationValues> extract(ClassFileAttributeInfo annotation) {
        if (annotation != null && "RuntimeVisibleAnnotations".equals(annotation.name)) {
            AnnotationData data = new AnnotationData(annotation.value);
            int numberOfAnnotations = data.nextInt();
            List<ClassFileAnnotationValues> annotations = new ArrayList<ClassFileAnnotationValues>(numberOfAnnotations);
            for (int i = 0; i < numberOfAnnotations; i++) {
                annotations.add(new ClassFileAnnotationValues(data));
            }
            return annotations;
        } else {
            return new ArrayList<ClassFileAnnotationValues>();
        }
    }

    static class ClassFileAnnotationValues extends ClassFileAnnotationInfo {
        final List<ClassFileAnnotationInfo> values;

        private ClassFileAnnotationValues(AnnotationData data) {
            super(data);
            this.values = extractValues(data, false);
        }

        private List<ClassFileAnnotationInfo> extractValues(AnnotationData data, boolean skip) {
            int numberOfPairs = data.nextInt();
            List<ClassFileAnnotationInfo> values = new ArrayList<ClassFileAnnotationInfo>(numberOfPairs);
            for (int i = 0; i < numberOfPairs; i++) {
                if (!skip) data.skipInt();
                byte tag = data.nextByte();
                if (tag == 'e') { // Enum
                    values.add(new ClassFileAnnotationInfo(data));
                    data.skipInt();
                } else if (tag == 'c') values.add(new ClassFileAnnotationInfo(data)); // Class
                else if (tag == '@') values.add(new ClassFileAnnotationValues(data)); // Annotation
                else if (tag == '[') values.addAll(extractValues(data, true)); // List
                else data.skipInt(); // Skip the rest
            }
            return values;
        }

    }

    private static class AnnotationData {
        private int index = 0;
        private byte[] data;

        public AnnotationData(byte[] data) {
            this.data = data;
        }

        int nextInt() {
            byte first = data[index++];
            byte seconds = data[index++];
            return (first << 8 & 0xFF00) | (seconds & 0xFF);
        }

        void skipInt() {
            index += 2;
        }

        byte nextByte() {
            return data[index++];
        }
    }
}
