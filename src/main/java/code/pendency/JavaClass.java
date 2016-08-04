package code.pendency;

import java.util.*;

public class JavaClass extends HasDependencies<String, JavaClass> {

    private final static int NO_MINOR_VERSION = 0;
    private final static int NO_MAJOR_VERSION = 0;
    private final static Set<String> NO_EFFERENT_IDS = Collections.emptySet();
    private final static Set<JavaClass> NO_EFFERENTS = Collections.emptySet();
    private final static Set<JavaClass> NO_AFFERENTS = Collections.emptySet();

    private final int minorVersion;
    private final int majorVersion;
    private final String className;
    private final String packageName;
    private final boolean isAbstract;
    private final boolean isInterface;
    private final String sourceFile;
    private final String jarName;
    private final Set<String> efferentIds;
    private final Set<JavaClass> efferents;
    private final Set<JavaClass> afferents;

    JavaClass(String className) {
        this(NO_MINOR_VERSION, NO_MAJOR_VERSION,
                className,
                classNameToPackageName(className),
                false,
                false,
                "Unknown",
                "",
                NO_EFFERENT_IDS, NO_EFFERENTS, NO_AFFERENTS);
    }

    JavaClass(int minorVersion,
                     int majorVersion,
                     String className,
                     String packageName,
                     boolean isAbstract,
                     boolean isInterface,
                     String sourceFile,
                     String jarName,
                     Set<String> efferentIds,
                     Set<JavaClass> efferents,
                     Set<JavaClass> afferents) {
        super(className, efferents, afferents);
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.className = className;
        this.packageName = packageName;
        this.isAbstract = isAbstract;
        this.isInterface = isInterface;
        this.sourceFile = sourceFile;
        this.jarName = jarName;
        this.efferentIds = efferentIds;
        this.efferents = efferents;
        this.afferents = afferents;
    }

    JavaClass(int minorVersion,
              int majorVersion,
              String className,
              String sourceFile,
              String jarName,
              boolean isAbstract,
              boolean isInterface,
              List<String> importedClasses) {
        this(minorVersion,
                majorVersion,
                className,
                classNameToPackageName(className),
                isAbstract,
                isInterface,
                sourceFile,
                jarName,
                toEfferents(importedClasses, className),
                NO_EFFERENTS,
                NO_AFFERENTS);
    }

    private static String classNameToPackageName(String className) {
        int index = className.lastIndexOf(".");
        return index != -1 ? className.substring(0, index) : "Default";
    }

    private static Set<String> toEfferents(Collection<String> efferents, String className) {
        Set<String> filtered = new HashSet<String>(efferents.size());
        for (String efferent : efferents) if (!efferent.equals(className)) {
            filtered.add(efferent);
        }
        return filtered;
    }

    JavaClass withEfferents(Set<JavaClass> efferents) {
        return new JavaClass(minorVersion, majorVersion, className, packageName, isAbstract, isInterface, sourceFile, jarName, efferentIds, efferents, afferents);
    }

    JavaClass withAfferents(Set<JavaClass> afferents) {
        return new JavaClass(minorVersion, majorVersion, className, packageName, isAbstract, isInterface, sourceFile, jarName, efferentIds, efferents, afferents);
    }

    Collection<String> getEfferentIds() {
        return efferentIds;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getJarName() {
        return jarName;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public String toString() {
        return className;
    }

}
