package code.pendency;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

abstract class HasDependencies<T, U extends HasDependencies<T, U>> {

    final T id;
    private final Set<U> efferents;
    private final Set<U> afferents;

    HasDependencies(T id) {
        this.id = id;
        this.efferents = Collections.emptySet();
        this.afferents = Collections.emptySet();
    }

    HasDependencies(T id, Set<U> efferents, Set<U> afferents) {
        this.id = id;
        this.efferents = Collections.unmodifiableSet(efferents);
        this.afferents = Collections.unmodifiableSet(afferents);
    }

    public Collection<U> getAfferents() {
        return afferents;
    }

    public Collection<U> getEfferents() {
        return efferents;
    }

    public int afferentCoupling() {
        return afferents.size();
    }

    public int efferentCoupling() {
        return efferents.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HasDependencies<?,?> that = (HasDependencies<?,?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
