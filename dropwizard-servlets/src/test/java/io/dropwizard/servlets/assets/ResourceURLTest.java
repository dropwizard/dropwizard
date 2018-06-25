package io.dropwizard.servlets.assets;

import io.dropwizard.util.Resources;
import org.junit.Test;

import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ResourceURLTest {

    @Test
    public void isDirectoryReturnsTrueForPlainDirectories() throws Exception {
        final Path path = Files.createTempDirectory("resource_url_test_dir");
        final URL url = path.toUri().toURL();

        assertThat(url.getProtocol())
                .isEqualTo("file");
        assertThat(ResourceURL.isDirectory(url))
                .isTrue();

        Files.delete(path);
    }

    @Test
    public void isDirectoryReturnsFalseForPlainFiles() throws Exception {
        final Path path = Files.createTempFile("resource_url_test", null);
        final URL url = path.toUri().toURL();

        assertThat(url.getProtocol())
                .isEqualTo("file");
        assertThat(ResourceURL.isDirectory(url))
                .isFalse();

        Files.delete(path);
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
        final URL url = Resources.getResource("META-INF/");
        final URL nurl = new URL(url.toExternalForm() + "missing");
        assertThatThrownBy(() -> ResourceURL.isDirectory(nurl))
            .isInstanceOf(ResourceNotFoundException.class);
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
    public void getLastModifiedReturnsTheLastModifiedTimeOfAFile() throws Exception {
        final Path path = Files.createTempFile("resource_url_test", null);
        final URL url = path.toUri().toURL();
        final long lastModified = ResourceURL.getLastModified(url);

        assertThat(lastModified)
                .isGreaterThan(0);
        assertThat(lastModified)
                .isEqualTo(path.toFile().lastModified());

        Files.delete(path);
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
