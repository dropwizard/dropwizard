package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.pool.PoolStats;

import java.util.concurrent.TimeUnit;

public class InstrumentedPoolingClientConnectionManager extends PoolingClientConnectionManager {

    public InstrumentedPoolingClientConnectionManager(final SchemeRegistry schreg) {
        this(schreg, -1, TimeUnit.MILLISECONDS);
    }

    public InstrumentedPoolingClientConnectionManager(final SchemeRegistry schreg,final DnsResolver dnsResolver) {
        this(schreg, -1, TimeUnit.MILLISECONDS,dnsResolver);
    }

    public InstrumentedPoolingClientConnectionManager() {
        this(SchemeRegistryFactory.createDefault());
    }

    public InstrumentedPoolingClientConnectionManager(
            final SchemeRegistry schemeRegistry,
            final long timeToLive, final TimeUnit tunit) {
        this(schemeRegistry, timeToLive, tunit, new SystemDefaultDnsResolver());
    }

    public InstrumentedPoolingClientConnectionManager(SchemeRegistry schemeRegistry, long timeToLive, TimeUnit tunit, DnsResolver dnsResolver) {
        super(schemeRegistry, timeToLive, tunit, dnsResolver);
        Metrics.newGauge(ClientConnectionManager.class,
                "available_connections",
                new Gauge<Integer>() {
                    @Override
                    public Integer value() {
                        PoolStats stats = getTotalStats();
                        return stats.getAvailable();
                    }
                });

        Metrics.newGauge(ClientConnectionManager.class,
                "leased_connections",
                new Gauge<Integer>() {
                    @Override
                    public Integer value() {
                        PoolStats stats = getTotalStats();
                        return stats.getLeased();
                    }
                });

        Metrics.newGauge(ClientConnectionManager.class,
                "pending_connections",
                new Gauge<Integer>() {
                    @Override
                    public Integer value() {
                        PoolStats stats = getTotalStats();
                        return stats.getPending();
                    }
                });
    }


}
