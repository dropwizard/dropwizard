package io.dropwizard.benchmarks.jersey;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class DropwizardResourceConfigBenchmark {

    private DropwizardResourceConfig dropwizardResourceConfig =
            new DropwizardResourceConfig(true, new MetricRegistry());

    @Setup
    public void setUp() {
        dropwizardResourceConfig.register(DistributionResource.class);
        dropwizardResourceConfig.register(AssetResource.class);
        dropwizardResourceConfig.register(ClustersResource.class);
    }

    @Benchmark
    public String getEndpointsInfo() {
        return dropwizardResourceConfig.getEndpointsInfo();
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
                .include(DropwizardResourceConfigBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10)
                .measurementIterations(5)
                .build())
                .run();
    }

    // Jersey resources (test data)

    @Path("assets")
    public static class AssetResource {

        @POST
        public String insert(String asset) {
            return "id";
        }

        @GET
        @Path("{id}")
        public String get(@PathParam("id") String id) {
            return "asset_by_id";
        }

        @GET
        @Path("{id}/details")
        public String getDetails(@PathParam("id") String id) {
            return "asset_details";
        }

        @GET
        public List<String> getAll() {
            return ImmutableList.of("first_asset", "second_asset");
        }

        @DELETE
        @Path("{id}")
        public void delete(@PathParam("id") String id) {
        }

        @PUT
        @Path("{id}")
        public void update(@PathParam("id") String id, String asset) {
        }
    }

    @Path("distributions")
    public static class DistributionResource {

        @POST
        @Path("{assetId}/clusters/{code}/start")
        public void start(@PathParam("assetId") String assetId,
                                      @PathParam("code") String code) {
        }

        @POST
        @Path("{assetId}/clusters/{code}/complete")
        public void complete(@PathParam("assetId") String assetId,
                                         @PathParam("code") String code) {
        }

        @POST
        @Path("{assetId}/clusters/{code}/abort")
        public void abort(@PathParam("assetId") String assetId,
                                      @PathParam("code") String code) {
        }

        @POST
        @Path("{assetId}/clusters/{code}/delete")
        public void delete(@PathParam("assetId") String assetId,
                                       @PathParam("code") String code) {
        }

        @GET
        @Path("{assetId}/clusters/{code}")
        public String getStatus(@PathParam("assetId") String assetId,
                                @PathParam("code") String code) {
            return "distributed";
        }
    }

    @Path("clusters")
    public static class ClustersResource {

        @POST
        public String insert(String cluster) {
            return "code";
        }

        @GET
        @Path("{code}")
        public String get(@PathParam("code") String code) {
            return "cluster_by_code";
        }

        @GET
        public List<String> getAll() {
            return ImmutableList.of("first_cluster", "second_cluster", "third_cluster");
        }

        @DELETE
        @Path("{code}")
        public void delete(@PathParam("code") String code) {
        }
    }
}
