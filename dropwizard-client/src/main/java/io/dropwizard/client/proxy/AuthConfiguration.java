package io.dropwizard.client.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Represents a configuration of credentials (username / password)
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code username}</td>
 *         <td>REQUIRED</td>
 *         <td>The username used to connect to the server.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code password}</td>
 *         <td>REQUIRED</td>
 *         <td>The password used to connect to the server.</td>
 *     </tr>
 * </table>
 */
public class AuthConfiguration {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    public AuthConfiguration() {
    }

    public AuthConfiguration(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }
}
