package org.skife.jdbi.v2;

/**
 * A dirty hack to get an instance of {@link BuiltInArgumentFactory}.
 */
public final class BuiltInArgumentFactoryFactory {
    /**
     * Get a new instance of {@link BuiltInArgumentFactory}.
     *
     * @return A new instance of {@link BuiltInArgumentFactory}.
     */
    public static BuiltInArgumentFactory create() {
        return new BuiltInArgumentFactory();
    }
}
