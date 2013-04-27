package com.yammer.dropwizard.views.flashscope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.NewCookie;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FlashOut extends Flash {

    public FlashOut() {
        super(Maps.<String, Object>newLinkedHashMap());
    }

    public NewCookie build(FlashScopeConfig config) {
        try {
            String unencodedJson = objectMapper.writeValueAsString(attributes);
            return newCookie(config.getCookieName(),
                            config.getCookiePath(),
                            config.getCookieDomain(),
                            (int) config.getCookieMaxAge().toSeconds(),
                            unencodedJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize flash attributes", e);
        }
    }

    public static NewCookie expireImmediately(FlashScopeConfig config) {
        return newCookie(config.getCookieName(),
                config.getCookiePath(),
                config.getCookieDomain(),
                0,
                "{}");
    }

    private static NewCookie newCookie(String name, String path, String domain, int maxAge, String content) {
        try {
            return new NewCookie(name,
                    URLEncoder.encode(content, "utf-8"),
                    path,
                    domain,
                    "",
                    maxAge,
                    false);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
