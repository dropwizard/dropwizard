package com.yammer.dropwizard.client;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.yammer.dropwizard.client.HttpClientConfiguration.DnsOverrides;

public class OverridingDnsResolver implements DnsResolver {

    private DnsOverrides overrides;
    private SystemDefaultDnsResolver systemResolver = new SystemDefaultDnsResolver();

    public OverridingDnsResolver(DnsOverrides overrides) {
        this.overrides = overrides;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        if (overrides.containsKey(host)) {
            return new InetAddress[] { overrides.lookUp(host) };
        }

        return systemResolver.resolve(host);
    }
}
