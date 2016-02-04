package io.dropwizard.servlets.assets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Helper methods for dealing with {@link URL} objects for local resources.
 */
public class ResourceURL {
    private ResourceURL() { /* singleton */ }

    /**
     * Returns true if the URL passed to it corresponds to a directory.  This is slightly tricky due to some quirks
     * of the {@link JarFile} API.  Only jar:// and file:// URLs are supported.
     *
     * @param resourceURL the URL to check
     * @return true if resource is a directory
     */
    public static boolean isDirectory(URL resourceURL) throws URISyntaxException {
        final String protocol = resourceURL.getProtocol();
        switch (protocol) {
            case "jar":
                try {
                    final JarURLConnection jarConnection = (JarURLConnection) resourceURL.openConnection();
                    final JarEntry entry = jarConnection.getJarEntry();
                    if (entry.isDirectory()) {
                        return true;
                    }

                    // WARNING! Heuristics ahead.
                    // It turns out that JarEntry#isDirectory() really just tests whether the filename ends in a '/'.
                    // If you try to open the same URL without a trailing '/', it'll succeed — but the result won't be
                    // what you want. We try to get around this by calling getInputStream() on the file inside the jar.
                    // This seems to return null for directories (though that behavior is undocumented as far as I
                    // can tell). If you have a better idea, please improve this.

                    final String fileName = resourceURL.getFile();
                    // leaves just the relative file path inside the jar
                    final String relativeFilePath = fileName.substring(fileName.lastIndexOf('!') + 2);
                    final JarFile jarFile = jarConnection.getJarFile();
                    final ZipEntry zipEntry = jarFile.getEntry(relativeFilePath);
                    final InputStream inputStream = jarFile.getInputStream(zipEntry);

                    return (inputStream == null);
                } catch (IOException e) {
                    throw new ResourceNotFoundException(e);
                }
            case "file":
                return new File(resourceURL.toURI()).isDirectory();
            default:
                throw new IllegalArgumentException("Unsupported protocol " + resourceURL.getProtocol() +
                        " for resource " + resourceURL);
        }
    }

    /**
     * Appends a trailing '/' to a {@link URL} object. Does not append a slash if one is already present.
     *
     * @param originalURL The URL to append a slash to
     * @return a new URL object that ends in a slash
     */
    public static URL appendTrailingSlash(URL originalURL) {
        try {
            return originalURL.getPath().endsWith("/") ? originalURL :
                    new URL(originalURL.getProtocol(),
                            originalURL.getHost(),
                            originalURL.getPort(),
                            originalURL.getFile() + '/');
        } catch (MalformedURLException ignored) { // shouldn't happen
            throw new IllegalArgumentException("Invalid resource URL: " + originalURL);
        }
    }

    /**
     * Returns the last modified time for file:// and jar:// URLs.  This is slightly tricky for a couple of reasons:
     * 1) calling getConnection on a {@link URLConnection} to a file opens an {@link InputStream} to that file that
     * must then be closed — though this is not true for {@code URLConnection}s to jar resources
     * 2) calling getLastModified on {@link JarURLConnection}s returns the last modified time of the jar file, rather
     * than the file within
     *
     * @param resourceURL the URL to return the last modified time for
     * @return the last modified time of the resource, expressed as the number of milliseconds since the epoch, or 0
     * if there was a problem
     */
    public static long getLastModified(URL resourceURL) {
        final String protocol = resourceURL.getProtocol();
        switch (protocol) {
            case "jar":
                try {
                    final JarURLConnection jarConnection = (JarURLConnection) resourceURL.openConnection();
                    final JarEntry entry = jarConnection.getJarEntry();
                    return entry.getTime();
                } catch (IOException ignored) {
                    return 0;
                }
            case "file":
                URLConnection connection = null;
                try {
                    connection = resourceURL.openConnection();
                    return connection.getLastModified();
                } catch (IOException ignored) {
                    return 0;
                } finally {
                    if (connection != null) {
                        try {
                            connection.getInputStream().close();
                        } catch (IOException ignored) {
                            // do nothing.
                        }
                    }
                }
            default:
                throw new IllegalArgumentException("Unsupported protocol " + resourceURL.getProtocol() + " for resource " + resourceURL);
        }
    }
}
