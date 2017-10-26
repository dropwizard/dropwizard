package io.dropwizard.logging.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents access logging attributes.
 */
public enum AccessAttribute {

    @JsonProperty("contentLength") CONTENT_LENGTH,
    @JsonProperty("method") METHOD,
    @JsonProperty("remoteAddress") REMOTE_ADDRESS,
    @JsonProperty("remoteUser") REMOTE_USER,
    @JsonProperty("requestTime") REQUEST_TIME,
    @JsonProperty("requestUri") REQUEST_URI,
    @JsonProperty("requestUrl") REQUEST_URL,
    @JsonProperty("statusCode") STATUS_CODE,
    @JsonProperty("protocol") PROTOCOL,
    @JsonProperty("remoteHost") REMOTE_HOST,
    @JsonProperty("serverName") SERVER_NAME,
    @JsonProperty("requestParameters") REQUEST_PARAMETERS,
    @JsonProperty("userAgent") USER_AGENT,
    @JsonProperty("localPort") LOCAL_PORT,
    @JsonProperty("requestContent") REQUEST_CONTENT,
    @JsonProperty("responseContent") RESPONSE_CONTENT,
    @JsonProperty("timestamp") TIMESTAMP;
}
