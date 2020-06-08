package io.dropwizard;

/**
 * Class that exposes the Dropwizard version. Fetches the "Implementation-Version"
 * manifest attribute from the jar file.
 * <p>
 * Note that some ClassLoaders do not expose the package metadata, hence this class might
 * not be able to determine the Dropwizard version in all environments. Consider using a
 * reflection-based check instead: For example, checking for the presence of a specific
 * Dropwizard method that you intend to call.
 */
public class DropwizardVersion {

    private DropwizardVersion() {
    }

    /**
     * Return the full version string of the present Dropwizard codebase, or {@code null}
     * if it cannot be determined.
     * @return the version of Dropwizard or {@code null}
     * @see Package#getImplementationVersion()
     */
    public static String getVersion() {
        Package pkg = DropwizardVersion.class.getPackage();
        return (pkg != null) ? pkg.getImplementationVersion() : null;
    }
}
