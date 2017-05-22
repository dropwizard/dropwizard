
package io.dropwizard;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A configuration source provider to be used for both local files and URLs.
 * 
 * @author JAshe
 */
public class MultiConfigurationSourceProvider implements ConfigurationSourceProvider {

     /**
     * Interface requires that this method be implemented. Should not be 
     * used when this implementation is being used.
     * 
     * @param path a path to a local config file
     * @return an InputStream pointing to the local file
     * @throws IOException if there is an error reading the file
     */
    @Override
    public InputStream open(String path) throws IOException {
        return open(path, false);
    }

    /**
     * Opens an InputStream pointing to either a file or a URL.
     * @param path      the path of the configuration file
     * @param isURL     true if the path points to a url, false if it points to
     *                  a local file
     * @return an InputStream pointing to a local file or the URL
     * @throws IOException  if there is an error reading the file
     */
    public InputStream open(String path, boolean isURL) throws IOException {

        if (isURL) {
            return new URL(path).openStream();
        } else {
            final File file = new File(path);
            if (!file.exists()) {
                throw new FileNotFoundException("File " + file + " not found");
            }

            return new FileInputStream(file);
        }
    }
}
