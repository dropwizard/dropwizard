package com.yammer.dropwizard.util.tests;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.yammer.dropwizard.util.ResourceURL;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * @author Sam Quigley <quigley@emerose.com>
 */
public class ResourceURLTest {
    private static File A_DIRECTORY;
    private static File A_FILE;

    @BeforeClass
    public static void setup() throws Exception {
        A_FILE = File.createTempFile("resource_url_test", null);
        A_FILE.deleteOnExit();

        A_DIRECTORY = Files.createTempDir();
        A_DIRECTORY.deleteOnExit();
    }

    @Test
    public void isDirectoryReturnsTrueForPlainDirectories() throws Exception {
        URL url = A_DIRECTORY.toURI().toURL();
        assertThat(url.getProtocol(), is("file"));
        assertThat(ResourceURL.isDirectory(url), is(true));
    }

    @Test
    public void isDirectoryReturnsFalseForPlainFiles() throws Exception {
        URL url = A_FILE.toURI().toURL();
        assertThat(url.getProtocol(), is("file"));
        assertThat(ResourceURL.isDirectory(url), is(false));
    }

    @Test
    public void isDirectoryReturnsTrueForDirectoriesInJars() {
        URL url = Resources.getResource("META-INF/");
        assertThat(url.getProtocol(), is("jar"));
        assertThat(ResourceURL.isDirectory(url), is(true));
    }

    @Test
    public void isDirectoryReturnsFalseForFilesInJars() {
        URL url = Resources.getResource("META-INF/MANIFEST.MF");
        assertThat(url.getProtocol(), is("jar"));
        assertThat(ResourceURL.isDirectory(url), is(false));
    }

    @Test
    public void isDirectoryReturnsTrueForDirectoriesInJarsWithoutTrailingSlashes() {
        URL url = Resources.getResource("META-INF");
        assertThat(url.getProtocol(), is("jar"));
        assertThat(ResourceURL.isDirectory(url), is(true));
    }

    @Test
    public void appendTrailingSlashAddsASlash() {
        URL url = Resources.getResource("META-INF");

        assertThat(url.toExternalForm(), not(endsWith("/")));
        assertThat(ResourceURL.appendTrailingSlash(url).toExternalForm(), endsWith("/"));
    }

    @Test
    public void appendTrailingSlashDoesntASlashWhenOneIsAlreadyPresent() {
        URL url = Resources.getResource("META-INF/");

        assertThat(url.toExternalForm(), endsWith("/"));
        assertThat(ResourceURL.appendTrailingSlash(url).toExternalForm(), not(endsWith("//")));
        assertThat(url, equalTo(ResourceURL.appendTrailingSlash(url)));
    }

    @Test
    public void resolveRelativeUrlResolvesPathsCorrectly() throws Exception {
        URL url = new URL("file:/example/directory/");
        URL newUrl = ResourceURL.resolveRelativeURL(url, "foo");

        assertThat(newUrl.toExternalForm(), is("file:/example/directory/foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveRelativeUrlThrowsExceptionsIfProtocolIsOverridden() throws Exception {
        URL url = new URL("file:/example/directory/");
        ResourceURL.resolveRelativeURL(url, "http://");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveRelativeUrlThrowsExceptionsIfAuthorityIsOverridden() throws Exception {
        URL url = new URL("file:/example/directory/");
        ResourceURL.resolveRelativeURL(url, "//foo/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveRelativeUrlThrowsExceptionsIfPathIsEscaped() throws Exception {
        URL url = new URL("file:/example/directory/");
        ResourceURL.resolveRelativeURL(url, "../../alternate/directory");
    }

    @Test
    public void getLastModifiedReturnsTheLastModifiedTimeOfAFile() throws Exception {
        URL url = A_FILE.toURI().toURL();
        long lastModified = ResourceURL.getLastModified(url);

        assertThat(lastModified, is(greaterThan(0L)));
        assertThat(lastModified, is(A_FILE.lastModified()));
    }

    @Test
    public void getLastModifiedReturnsTheLastModifiedTimeOfAJarEntry() throws Exception {
        URL url = Resources.getResource("META-INF/MANIFEST.MF");
        long lastModified = ResourceURL.getLastModified(url);

        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        JarEntry entry = jarConnection.getJarEntry();

        assertThat(lastModified, is(greaterThan(0L)));
        assertThat(lastModified, is(entry.getTime()));
    }

    @Test
    public void getLastModifiedReturnsZeroIfAnErrorOccurs() throws Exception {
        URL url = new URL("file:/some/path/that/doesnt/exist");
        long lastModified = ResourceURL.getLastModified(url);

        assertThat(lastModified, is(0L));
    }
}
