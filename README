# Codependency

Codependency is a project to analyse dependency between the classfile of a JVM.

## Inspiration

This project has been strongly inspired by the [JDepend](https://github.com/clarkware/jdepend) project.

## How does it work?

The project can analyses the entire classpath, a directories or jar file (zip or war are valid too).

    Analysis analysis = Codependency.create().withEntireClassPath().analyze();
    int numberOfClasses = analysis.numberOfClasses();
    Collection<JavaClass> classes = analysis.getClasses();

The result of the analysis contains the list of Java classes found.

    Analysis analysis = Codependency.create().withDirectory("/absolute/path").analyze();
    // or
    Analysis analysis = Codependency.create().withDirectory("/path/to/file.jar").analyze();

It is also possible to filter classes from the result...

    Analysis analysis = Codependency.create()
        .withEntireClassPath()
        .includes("class.prefix.to.Include")
        .excludes("class.prefix.to.Include")
        .analyze();

... and decide to include the inner classes or not (by default they are included).

    Analysis analysis = Codependency.create()
        .withEntireClassPath()
        .includesInnerClasses()
        .analyze();
    // or
    Analysis analysis = Codependency.create()
        .withEntireClassPath()
        .excludesInnerClasses()
        .analyze();

The result of the analysis contains a collections of Java classes where each item contains information about the class.

    ...
    JavaClass javaClass = classes.iterator().next();
    javaClass.getClassName();
    javaClass.getPackageName();
    javaClass.getMinorVersion(); // Java minor version
    javaClass.getMajorVersion(); // Java major version
    javaClass.getSourceFile(); // The name of the source file where the class was defined
    javaClass.getJarName(); // The name of the jar file that contains the class, if doesn't exist is "<source_code>"
    javaClass.isAbstract();
    javaClass.isInterface();
    javaClass.getEfferents(); // The collection of classes the class depends on
    javaClass.efferentCoupling(); // The number of efferents
    javaClass.getAfferents(); // The collection of classes depending on this class
    javaClass.afferentCoupling(); // The number of afferents

The analysis can be group by a property of the class. For example the package name.

    ...
    Collection<GroupByItem<String>> groupByPackage = analysis.group(Analysis.BY_PACKAGE);
    GroupByItem<String> item = groupByPackage.iterator().next();
    item.getName(); // The group name, in this case the package name
    item.getCount(); // The number of classes inside the group, in this case the package
    item.getEfferents(); // The collection of classes the class depends on
    item.efferentCoupling(); // The number of efferents
    item.getAfferents(); // The collection of classes depending on this class
    item.afferentCoupling(); // The number of afferents

But it possible to group by all the class property for example check the dependencies between jar file.

    ...
    Collection<GroupByItem<String>> groupByJarName = analysis.group(new GroupById<String>() {
        @Override
        public String extract(JavaClass javaClass) {
            return javaClass.getJarName();
        }
    });