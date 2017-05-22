
package io.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.File;
import java.net.URL;

/**
 * Services that use merged (inherited) configuration files should have their 
 * configuration class extend this class.
 * 
 * @author JAshe
 */
public class MergedConfiguration extends Configuration {
    private File parentConfigurationFile;
    private URL parentConfigurationURL;

    @JsonProperty("parentConfigurationURL")
    public URL getParentConfigurationURL() {
        return parentConfigurationURL;
    }

    @JsonProperty("parentConfigurationURL")
    public void setParentConfigurationURL(URL parentConfigurationURL) {
        this.parentConfigurationURL = parentConfigurationURL;
    }
    
    @JsonProperty("parentConfigurationFile")
    public File getParentConfigurationFile() {
        return parentConfigurationFile;
    }
    
    @JsonProperty("parentConfigurationFile")
    public void setParentConfigurationFile(File parentFile) {
        this.parentConfigurationFile = parentFile;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                      .add("server", super.getServerFactory())
                      .add("parentConfigurationFile", parentConfigurationFile)
                      .toString();
    }
}
