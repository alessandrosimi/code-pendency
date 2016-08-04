package code.pendency;

import java.io.*;
import java.util.*;

class FileManager {

    private final List<File> directories;
    private final Filter filter;

    FileManager(Filter filter, List<String> directories) {
        this.filter = filter;
        List<File> files = new ArrayList<File>();
        for (String directoryName : directories) {
            File file = createDirectory(directoryName);
            if (file != null) files.add(file);
        }
        this.directories = Collections.unmodifiableList(files);
    }

    private File createDirectory(String name) {
        File directory = new File(name);
        if (directory.isDirectory() || acceptJarFile(directory)) {
            return directory;
        } else {
            return null;
        }
    }

    public Collection<ExtractedFile> extractFiles() {
        Map<String, ExtractedFile> files = new HashMap<String, ExtractedFile>();
        for (File directory : directories) {
            collectFiles(directory, files);
        }
        return files.values();
    }

    private void collectFiles(File directory, Map<String, ExtractedFile> files) {
        if (directory.isFile()) {
            addFile(directory, files);
        } else {
            String[] directoryFiles = directory.list();
            for (String directoryFile : directoryFiles) {
                File file = new File(directory, directoryFile);
                if (file.isDirectory()) {
                    collectFiles(file, files);
                } else {
                    addFile(file, files);
                }
            }
        }
    }

    private void addFile(File f, Map<String, ExtractedFile> files) {
        ExtractedFile file = null;
        if (acceptClassFile(f)) {
            file = new ClassFile(f);
        } else if (acceptJarFile(f)) {
            file = new JarFile(f);
        }
        if (file != null && !files.containsKey(f.getAbsolutePath())) {
            files.put(f.getAbsolutePath(), file);
        }
    }

    private boolean acceptClassFile(File file) {
        return file.isFile() && acceptClassFileName(file.getName());
    }

    private boolean acceptClassFileName(String name) {
        return isClassFile(name) && filter.acceptInnerClass(name);
    }

    private boolean isClassFile(String name) {
        return name.toLowerCase().endsWith(".class");
    }

    private boolean acceptJarFile(File file) {
        return isJar(file) || isZip(file) || isWar(file);
    }

    private boolean isWar(File file) {
        return existsWithExtension(file, ".war");
    }

    private boolean isZip(File file) {
        return existsWithExtension(file, ".zip");
    }
 
    private boolean isJar(File file) {
        return existsWithExtension(file, ".jar");
    }

    private boolean existsWithExtension(File file, String extension) {
        return file.isFile() &&
            file.getName().toLowerCase().endsWith(extension);
    }

    static abstract class ExtractedFile {
        final File file;
        private ExtractedFile(File file) {
            this.file = file;
        }
    }

    static class ClassFile extends ExtractedFile {
        ClassFile(File file) {
            super(file);
        }
    }

    static class JarFile extends ExtractedFile {
        JarFile(File file) {
            super(file);
        }
    }

}