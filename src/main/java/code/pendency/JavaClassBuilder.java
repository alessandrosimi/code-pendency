package code.pendency;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

class JavaClassBuilder {

    private final ClassFileParser parser;
    private final FileManager fileManager;
    private final Filter filter;

    public JavaClassBuilder(ClassFileParser parser, FileManager fm, Filter filter) {
        this.parser = parser;
        this.fileManager = fm;
        this.filter = filter;
    }

    Collection<JavaClass> build() {
        Collection<JavaClass> classes = new ArrayList<JavaClass>();
        for (FileManager.ExtractedFile file : fileManager.extractFiles()) {
            try {
                Collection<JavaClass> builtClasses = buildClasses(file);
                classes.addAll(filter(builtClasses));
            } catch (IOException ioe) {
                System.err.println("\n" + ioe.getMessage());
            }
        }
        return classes;
    }

    private Collection<JavaClass> buildClasses(FileManager.ExtractedFile extracted) throws IOException {
        if (extracted instanceof FileManager.ClassFile) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(extracted.file));
                JavaClass parsedClass = parser.parse(is);
                Collection<JavaClass> javaClasses = new ArrayList<JavaClass>();
                javaClasses.add(parsedClass);
                return javaClasses;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } else if (extracted instanceof FileManager.JarFile) {
            JarFile jarFile = new JarFile(extracted.file);
            Collection<JavaClass> result = buildClasses(jarFile, extracted.file.getName());
            jarFile.close();
            return result;
        } else {
            throw new IOException("File is not a valid .class, .jar, .war, or .zip file: " + extracted.file.getPath());
        }
    }

    private Collection<JavaClass> buildClasses(JarFile file, String jarName) throws IOException {
        Collection<JavaClass> javaClasses = new ArrayList<JavaClass>();
        Enumeration entries = file.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = (ZipEntry) entries.nextElement();
            String name = e.getName();
            if (isClassFile(name) && acceptInnerClass(e.getName())) {
                InputStream is = null;
                try {
	                is = new BufferedInputStream(file.getInputStream(e));
                    JavaClass jc = parser.parse(is, jarName);
                    javaClasses.add(jc);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        }
        return javaClasses;
    }

    private boolean isClassFile(String name) {
        return name.toLowerCase().endsWith(".class");
    }

    private boolean acceptInnerClass(String name) {
        return filter.acceptInnerClass(name);
    }

    private Collection<JavaClass> filter(Collection<JavaClass> classes) {
        Collection<JavaClass> filtered = new ArrayList<JavaClass>();
        for (JavaClass javaClass : classes) {
            if (filter.accept(javaClass.getClassName())) filtered.add(javaClass);
        }
        return filtered;
    }

}
