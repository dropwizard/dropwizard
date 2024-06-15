package io.dropwizard.servlets.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.jar.JarEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ResourceURLTest {
    private final URL resourceJar = getClass().getResource("/resources.jar");

    @Test
    void isDirectoryReturnsTrueForPlainDirectories(@TempDir Path tempDir) throws Exception {
        final URL url = tempDir.toUri().toURL();

        assertThat(url.getProtocol()).isEqualTo("file");
        assertThat(ResourceURL.isDirectory(url)).isTrue();
    }

    @Test
    void isDirectoryReturnsFalseForPlainFiles(@TempDir Path tempDir) throws Exception {
        final File tempFile = tempDir.resolve("resource_url_test").toFile();
        assumeTrue(tempFile.createNewFile());

        final URL url = tempFile.toURI().toURL();

        assertThat(url.getProtocol()).isEqualTo("file");
        assertThat(ResourceURL.isDirectory(url)).isFalse();
    }

    @Test
    void isDirectoryReturnsTrueForDirectoriesInJars() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/dir/");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isTrue();
    }

    @Test
    void isDirectoryReturnsTrueForDirectoriesWithSpacesInJars() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/dir with space/");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isTrue();
    }

    @Test
    void isDirectoryReturnsTrueForURLEncodedDirectoriesInJars() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/dir%20with%20space/");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isTrue();
    }

    @Test
    void isDirectoryReturnsFalseForFilesInJars() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/file.txt");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isFalse();
    }

    @Test
    void isDirectoryReturnsFalseForFilesWithSpacesInJars() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/file with space.txt");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isFalse();
    }

    @Test
    void isDirectoryReturnsFalseForURLEncodedFilesInJars() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/file%20with%20space.txt");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isFalse();
    }

    @Test
    void isDirectoryReturnsTrueForDirectoriesInJarsWithoutTrailingSlashes() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/dir");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isTrue();
    }

    @Test
    void isDirectoryReturnsTrueForDirectoriesWithSpacesInJarsWithoutTrailingSlashes() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/dir with space");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isTrue();
    }

    @Test
    void isDirectoryReturnsTrueForURLEncodedDirectoriesInJarsWithoutTrailingSlashes() throws Exception {
        final URL url = new URL("jar:" + resourceJar.toExternalForm() + "!/dir%20with%20space");

        assertThat(url.getProtocol()).isEqualTo("jar");
        assertThat(ResourceURL.isDirectory(url)).isTrue();
    }

    @Test
    void isDirectoryThrowsResourceNotFoundExceptionForMissingDirectories() throws Exception {
        final URL url = getClass().getResource("/META-INF/");
        final URL nurl = new URL(url.toExternalForm() + "missing");
        assertThatExceptionOfType(ResourceNotFoundException.class)
            .isThrownBy(() -> ResourceURL.isDirectory(nurl));
    }

    @Test
    void appendTrailingSlashAddsASlash() {
        final URL url = getClass().getResource("/META-INF");

        assumeThat(url.toExternalForm())
                .doesNotMatch(".*/$");
        assertThat(ResourceURL.appendTrailingSlash(url).toExternalForm())
                .endsWith("/");
    }

    @Test
    void appendTrailingSlashDoesntASlashWhenOneIsAlreadyPresent() {
        final URL url = getClass().getResource("/META-INF/");

        assertThat(url.toExternalForm())
                .endsWith("/");
        assertThat(ResourceURL.appendTrailingSlash(url).toExternalForm())
                .doesNotMatch(".*//$");
        assertThat(url)
                .isEqualTo(ResourceURL.appendTrailingSlash(url));
    }

    @Test
    void getLastModifiedReturnsTheLastModifiedTimeOfAFile(@TempDir Path tempDir) throws Exception {
        final URL url = tempDir.toUri().toURL();
        final long lastModified = ResourceURL.getLastModified(url);

        assertThat(lastModified)
                .isPositive()
                .isEqualTo(tempDir.toFile().lastModified());
    }

    @Test
    void getLastModifiedReturnsTheLastModifiedTimeOfAJarEntry() throws Exception {
        final URL url = getClass().getResource("/META-INF/MANIFEST.MF");
        final long lastModified = ResourceURL.getLastModified(url);

        final JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        final JarEntry entry = jarConnection.getJarEntry();

        assertThat(lastModified)
                .isPositive()
                .isEqualTo(entry.getTime());
    }

    @Test
    void getLastModifiedReturnsZeroIfAnErrorOccurs() throws Exception {
        final URL url = new URL("file:/some/path/that/doesnt/exist");
        final long lastModified = ResourceURL.getLastModified(url);

        assertThat(lastModified)
                .isZero();
    }
}
