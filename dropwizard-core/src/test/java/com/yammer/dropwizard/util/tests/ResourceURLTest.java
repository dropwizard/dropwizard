package com.yammer.dropwizard.util.tests;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.yammer.dropwizard.util.ResourceNotFoundException;
import com.yammer.dropwizard.util.ResourceURL;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class ResourceURLTest {
    private File directory;
    private File file;

    @Before
    public void setup() throws Exception {
        file = File.createTempFile("resource_url_test", null);
        file.deleteOnExit();

        directory = Files.createTempDir();
        directory.deleteOnExit();
    }

    @Test
    public void isDirectoryReturnsTrueForPlainDirectories() throws Exception {
        final URL url = directory.toURI().toURL();

        assertThat(url.getProtocol())
                .isEqualTo("file");
        assertThat(ResourceURL.isDirectory(url))
                .isTrue();
    }

    @Test
    public void isDirectoryReturnsFalseForPlainFiles() throws Exception {
        final URL url = file.toURI().toURL();

        assertThat(url.getProtocol())
                .isEqualTo("file");
        assertThat(ResourceURL.isDirectory(url))
                .isFalse();
    }

    @Test
    public void isDirectoryReturnsTrueForDirectoriesInJars() throws Exception {
        final URL url = Resources.getResource("META-INF/");

        assertThat(url.getProtocol())
                .isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url))
                .isTrue();
    }

    @Test
    public void isDirectoryReturnsFalseForFilesInJars() throws Exception {
        final URL url = Resources.getResource("META-INF/MANIFEST.MF");

        assertThat(url.getProtocol())
                .isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url))
                .isFalse();
    }

    @Test
    public void isDirectoryReturnsTrueForDirectoriesInJarsWithoutTrailingSlashes() throws Exception {
        final URL url = Resources.getResource("META-INF");

        assertThat(url.getProtocol())
                .isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url))
                .isTrue();
    }

    @Test
    public void isDirectoryThrowsResourceNotFoundExceptionForMissingDirectories() throws Exception {
        URL url = Resources.getResource("META-INF/");
        url = new URL(url.toExternalForm() + "missing");
        try {
            ResourceURL.isDirectory(url);
            fail("should have thrown an exception");
        }
        catch (ResourceNotFoundException ignored) {
            // expected
        }
    }

    @Test
    public void appendTrailingSlashAddsASlash() throws Exception {
        final URL url = Resources.getResource("META-INF");

        assertThat(url.toExternalForm())
                .doesNotMatch(".*/$");
        assertThat(ResourceURL.appendTrailingSlash(url).toExternalForm())
                .endsWith("/");
    }

    @Test
    public void appendTrailingSlashDoesntASlashWhenOneIsAlreadyPresent() throws Exception {
        final URL url = Resources.getResource("META-INF/");

        assertThat(url.toExternalForm())
                .endsWith("/");
        assertThat(ResourceURL.appendTrailingSlash(url).toExternalForm())
                .doesNotMatch(".*//$");
        assertThat(url)
                .isEqualTo(ResourceURL.appendTrailingSlash(url));
    }

    @Test
    public void resolveRelativeUrlResolvesPathsCorrectly() throws Exception {
        final URL url = new URL("file:/example/directory/");
        final URL newUrl = ResourceURL.resolveRelativeURL(url, "foo");

        assertThat(newUrl.toExternalForm())
                .isEqualTo("file:/example/directory/foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveRelativeUrlThrowsExceptionsIfProtocolIsOverridden() throws Exception {
        final URL url = new URL("file:/example/directory/");
        ResourceURL.resolveRelativeURL(url, "http://");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveRelativeUrlThrowsExceptionsIfAuthorityIsOverridden() throws Exception {
        final URL url = new URL("file:/example/directory/");
        ResourceURL.resolveRelativeURL(url, "//foo/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveRelativeUrlThrowsExceptionsIfPathIsEscaped() throws Exception {
        final URL url = new URL("file:/example/directory/");
        ResourceURL.resolveRelativeURL(url, "../../alternate/directory");
    }

    @Test
    public void getLastModifiedReturnsTheLastModifiedTimeOfAFile() throws Exception {
        final URL url = file.toURI().toURL();
        final long lastModified = ResourceURL.getLastModified(url);

        assertThat(lastModified)
                .isGreaterThan(0);
        assertThat(lastModified)
                .isEqualTo(file.lastModified());
    }

    @Test
    public void getLastModifiedReturnsTheLastModifiedTimeOfAJarEntry() throws Exception {
        final URL url = Resources.getResource("META-INF/MANIFEST.MF");
        final long lastModified = ResourceURL.getLastModified(url);

        final JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        final JarEntry entry = jarConnection.getJarEntry();

        assertThat(lastModified)
                .isGreaterThan(0);
        assertThat(lastModified)
                .isEqualTo(entry.getTime());
    }

    @Test
    public void getLastModifiedReturnsZeroIfAnErrorOccurs() throws Exception {
        final URL url = new URL("file:/some/path/that/doesnt/exist");
        final long lastModified = ResourceURL.getLastModified(url);

        assertThat(lastModified)
                .isZero();
    }
}
