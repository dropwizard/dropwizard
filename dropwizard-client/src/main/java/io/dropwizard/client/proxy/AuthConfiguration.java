package io.dropwizard.client.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * Represents a configuration of credentials for either Username Password or NT credentials
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
 *     <tr>
 *         <td>{@code authScheme}</td>
 *         <td>null</td>
 *         <td>Optional, The authentication scheme used by the underlying
 *         {@link org.apache.http.auth.AuthScope} class. Can be one of:<ul>
 *         <li>Basic</li><li>NTLM</li></ul></td>
 *     </tr>
 *     <tr>
 *         <td>{@code realm}</td>
 *         <td>null</td>
 *         <td>Optional, Realm to be used for NTLM Authentication.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code hostname}</td>
 *         <td>null</td>
 *         <td>The hostname of the Principal in NTLM Authentication.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code domain}</td>
 *         <td>null</td>
 *         <td>Optional, The domain used in NTLM Authentication.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code credentialType}</td>
 *         <td>null</td>
 *         <td>The {@link org.apache.http.auth.Credentials} implementation
 *         to use for proxy authentication. Currently supports
 *         UsernamePassword ({@link org.apache.http.auth.UsernamePasswordCredentials}) and
 *         NT ({@link org.apache.http.auth.NTCredentials})</td>
 *     </tr>
 * </table>
 */
public class AuthConfiguration {

    public static final String BASIC_AUTH_SCHEME = "Basic";

    public static final String NTLM_AUTH_SCHEME = "NTLM";

    public static final String USERNAME_PASSWORD_CREDS = "UsernamePassword";

    public static final String NT_CREDS = "NT";

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @Pattern(regexp = BASIC_AUTH_SCHEME + "|" + NTLM_AUTH_SCHEME)
    private String authScheme;

    private String realm;

    private String hostname;

    private String domain;

    @Pattern(regexp = USERNAME_PASSWORD_CREDS + "|" + NT_CREDS, flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String credentialType;

    public AuthConfiguration() {
    }

    public AuthConfiguration(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AuthConfiguration(String username, String password, String authScheme, String realm, String hostname, String domain, String credentialType) {
        this.username = username;
        this.password = password;
        this.authScheme = authScheme;
        this.realm = realm;
        this.hostname = hostname;
        this.domain = domain;
        this.credentialType = credentialType;
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

    @JsonProperty
    public String getAuthScheme() {
        return authScheme;
    }

    @JsonProperty
    public void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    @JsonProperty
    public String getRealm() {
        return realm;
    }

    @JsonProperty
    public void setRealm(String realm) {
        this.realm = realm;
    }

    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    @JsonProperty
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @JsonProperty
    public String getDomain() {
        return domain;
    }

    @JsonProperty
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @JsonProperty
    public String getCredentialType() {
        return credentialType;
    }

    @JsonProperty
    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType;
    }
}
