package code.pendency;

import java.util.List;

class Filter {

    final boolean includeInnerClasses;
    final List<String> includes;
    final List<String> excludes;

    Filter(boolean includeInnerClasses, List<String> includes, List<String> excludes) {
        this.includeInnerClasses = includeInnerClasses;
        this.includes = includes;
        this.excludes = excludes;
    }

    boolean acceptInnerClass(String name) {
        return includeInnerClasses || !isInnerClass(name);
    }

    private boolean isInnerClass(String name) {
        return name.toLowerCase().indexOf("$") > 0;
    }

    boolean accept(String className) {
        return acceptClass(className) && (includeInnerClasses || !isInnerClass(className));
    }

    private boolean acceptClass(String className) {
        boolean accept = true;
        if (!includes.isEmpty()) {
            accept = false;
            for (String include : includes) if (className.startsWith(include)) {
                accept = true;
            }
        }
        for (String exclude : excludes) if (className.startsWith(exclude)) {
            accept = false;
        }
        return accept;
    }

}
