package com.yammer.dropwizard.core;

import com.google.common.hash.Hashing;

import java.util.Calendar;
import java.util.Date;

public class CachedAsset {
    
    private final byte[] resource;
    
    private final String etag;
    
    private final Date lastModified;

    public CachedAsset(byte[] resource) {
        this.resource = resource.clone();

        this.etag = Hashing.murmur3_128().hashBytes(resource).toString();

        // lazy and non-aggressive lastModified impl
        // zero out the millis since the date we get back from If-Modified-Since will not have them
        Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND, 0);
        this.lastModified = now.getTime();
    }
    
    public byte[] getResource() {
        return resource.clone();
    }

    public String getEtag() {
        return etag;
    }

    public Date getLastModified() {
        return new Date(lastModified.getTime());
    }

}
