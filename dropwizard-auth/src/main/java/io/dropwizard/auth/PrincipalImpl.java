package io.dropwizard.auth;

import java.security.Principal;
import java.util.Objects;
import com.google.common.base.MoreObjects;

public class PrincipalImpl implements Principal {
    private final String name;

    public PrincipalImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PrincipalImpl principal = (PrincipalImpl) o;
        return Objects.equals(this.name, principal.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name",  name).toString();
    }
}
