package code.pendency;

import java.util.*;

public class Analysis {

    private final Set<JavaClass> classes;

    public Analysis(Set<JavaClass> classes) {
        this.classes = Collections.unmodifiableSet(classes);
    }

    public Collection<JavaClass> getClasses() {
        return classes;
    }

    public int numberOfClasses() {
        return classes.size();
    }

    public <T> Collection<GroupByItem<T>> group(GroupById<T> extractor) {
        Map<T, List<JavaClass>> groupBy = groupById(classes, extractor);
        Map<T, GroupByItem<T>> result = new HashMap<T, GroupByItem<T>>();
        for (Map.Entry<T, List<JavaClass>> entry : groupBy.entrySet()) {
            T id = entry.getKey();
            List<JavaClass> javaClasses = entry.getValue();
            GroupByItem<T> group = groupClasses(javaClasses, extractor, id);
            result.put(id, group);
        }
        List<GroupByItem<T>> items = new ArrayList<GroupByItem<T>>(result.size());
        for (GroupByItem<T> item : result.values()) items.add(item.withMap(result));
        return items;
    }

    private <T> Map<T, List<JavaClass>> groupById(Collection<JavaClass> classes, GroupById<T> extractor) {
        Map<T, List<JavaClass>> groupBy = new HashMap<T, List<JavaClass>>();
        for (JavaClass javaClass : classes) {
            T key = extractor.extract(javaClass);
            List<JavaClass> javaClasses = groupBy.get(key);
            if (javaClasses == null) {
                javaClasses = new ArrayList<JavaClass>();
                groupBy.put(key, javaClasses);
            }
            javaClasses.add(javaClass);
        }
        return groupBy;
    }

    private <T> GroupByItem<T> groupClasses(List<JavaClass> classes, GroupById<T> extractor, T id) {
        Set<T> efferents = new HashSet<T>();
        Set<T> afferents = new HashSet<T>();
        for (JavaClass javaClass : classes) {
            for (JavaClass afferent : javaClass.getAfferents()) {
                T afferentKey = extractor.extract(afferent);
                if (!id.equals(afferentKey)) {
                    afferents.add(afferentKey);
                }
            }
            for (JavaClass efferent : javaClass.getEfferents()) {
                T efferentKey = extractor.extract(efferent);
                if (!id.equals(efferentKey)) {
                    efferents.add(efferentKey);
                }
            }
        }
        return new GroupByItem<T>(classes.size(), efferents, afferents, id);
    }

    public final static GroupById<String> BY_PACKAGE = new GroupById<String>() {
        @Override
        public String extract(JavaClass javaClass) {
            return javaClass.getPackageName();
        }
    };

    public interface GroupById<T> {
        T extract(JavaClass javaClass);
    }

    public static class GroupByItem<T> extends HasDependencies<T, GroupByItem<T>> {

        private final int count;
        private final Set<T> efferents;
        private final Set<T> afferents;

        private GroupByItem(int count, Set<T> efferents, Set<T> afferents, T id) {
            super(id);
            this.count = count;
            this.efferents = efferents;
            this.afferents = afferents;
        }

        private GroupByItem(int count, T id, Set<GroupByItem<T>> efferents, Set<GroupByItem<T>> afferents) {
            super(id, efferents, afferents);
            this.count = count;
            this.efferents = Collections.emptySet();
            this.afferents = Collections.emptySet();
        }

        public T getName() {
            return id;
        }

        public int getCount() {
            return count;
        }

        private GroupByItem<T> withMap(Map<T, GroupByItem<T>> map) {
            return new GroupByItem<T>(count, id, fromMap(efferents, map), fromMap(afferents, map));
        }

        private Set<GroupByItem<T>> fromMap(Set<T> ids, Map<T, GroupByItem<T>> map) {
            Set<GroupByItem<T>> items = new HashSet<GroupByItem<T>>(ids.size());
            for (T id : ids) if (map.containsKey(id)) items.add(map.get(id));
            return items;
        }

    }

}
