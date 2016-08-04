package code.pendency;

import java.io.File;
import java.util.*;

public class Codependency {

    private final List<String> directories;
    private final boolean includeInnerClasses;
    private final List<String> includes;
    private final List<String> excludes;
    private final List<ParserListener> listeners;

    private Codependency(List<String> directories,
                         boolean includeInnerClasses,
                         List<String> includes,
                         List<String> excludes,
                         List<ParserListener> listeners) {
        this.directories = Collections.unmodifiableList(directories);
        this.includeInnerClasses = includeInnerClasses;
        this.includes = Collections.unmodifiableList(includes);
        this.excludes = Collections.unmodifiableList(excludes);
        this.listeners = Collections.unmodifiableList(listeners);
    }

    private final static List<String> empty = Collections.emptyList();
    private final static List<ParserListener> noListeners = Collections.emptyList();

    public static Codependency create() {
        return new Codependency(empty, true, empty, empty, noListeners);
    }

    public Codependency includesInnerClasses() {
        return new Codependency(directories, true, includes, excludes, listeners);
    }

    public Codependency excludesInnerClasses() {
        return new Codependency(directories, false, includes, excludes, listeners);
    }

    public Codependency withEntireClassPath() {
        String classpath = System.getProperty("java.class.path");
        String sunClassPath = System.getProperty("sun.boot.class.path");
        Set<String> directories = new HashSet<String>(this.directories);
        if (classpath != null) Collections.addAll(directories, classpath.split(File.pathSeparator));
        if (sunClassPath != null) Collections.addAll(directories, sunClassPath.split(File.pathSeparator));
        return new Codependency(new ArrayList<String>(directories), includeInnerClasses, includes, excludes, listeners);
    }

    public Codependency withDirectory(String name) {
        List<String> directories = new ArrayList<String>(this.directories);
        directories.add(name);
        return new Codependency(directories, includeInnerClasses, includes, excludes, listeners);
    }

    public Codependency withListener(ParserListener listener) {
        List<ParserListener> listeners = new ArrayList<ParserListener>(this.listeners);
        listeners.add(listener);
        return new Codependency(directories, includeInnerClasses, includes, excludes, listeners);
    }

    public Codependency excludes(String prefix) {
        List<String> excludes = new ArrayList<String>(this.excludes);
        excludes.add(prefix);
        return new Codependency(directories, includeInnerClasses, includes, excludes, listeners);
    }

    public Codependency includes(String prefix) {
        List<String> includes = new ArrayList<String>(this.includes);
        includes.add(prefix);
        return new Codependency(directories, includeInnerClasses, includes, excludes, listeners);
    }

    public Analysis analyze() {
        Collection<JavaClass> javaClasses = createBuilder().build();
        Map<String, JavaClass> efferentsMap = new HashMap<String, JavaClass>(javaClasses.size());
        Map<String, Set<String>> afferentsMap = new HashMap<String, Set<String>>(javaClasses.size());
        for (JavaClass javaClass : javaClasses) {
            efferentsMap.put(javaClass.getClassName(), javaClass);
            for (String efferentId : javaClass.getEfferentIds()) {
                if (!afferentsMap.containsKey(efferentId)) {
                    afferentsMap.put(efferentId, new HashSet<String>());
                }
                afferentsMap.get(efferentId).add(javaClass.getClassName());
                if (!efferentsMap.containsKey(efferentId)) {
                    efferentsMap.put(efferentId, new JavaClass(efferentId));
                }
            }
        }
        Set<JavaClass> result = new HashSet<JavaClass>(efferentsMap.size());
        for (JavaClass javaClass : efferentsMap.values()) {
            Set<JavaClass> efferents = new HashSet<JavaClass>(javaClass.getEfferentIds().size());
            for (String efferentId : javaClass.getEfferentIds()) {
                efferents.add(efferentsMap.get(efferentId));
            }
            javaClass = javaClass.withEfferents(efferents);
            Set<String> afferentIds = afferentsMap.get(javaClass.getClassName());
            if (afferentIds != null) {
                Set<JavaClass> afferents = new HashSet<JavaClass>(afferentIds.size());
                for (String afferentId : afferentIds) {
                    afferents.add(efferentsMap.get(afferentId));
                }
                javaClass = javaClass.withAfferents(afferents);
            }
            result.add(javaClass);
        }
        return new Analysis(result);
    }

    private JavaClassBuilder createBuilder() {
        Filter filter = new Filter(includeInnerClasses, includes, excludes);
        FileManager fileManager = new FileManager(filter, directories);
        ClassFileParser parser = new ClassFileParser(filter, listeners);
        return new JavaClassBuilder(parser, fileManager, filter);
    }

}
