package io.dropwizard.benchmarks.jersey;

import io.dropwizard.jersey.DropwizardResourceConfig;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.test.JerseyTest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class DropwizardResourceConfigBenchmark {

    private final DropwizardResourceConfig dropwizardResourceConfig = DropwizardResourceConfig.forTesting();

    @Setup
    public void setUp() throws Exception {
        dropwizardResourceConfig.register(DistributionResource.class);
        dropwizardResourceConfig.register(AssetResource.class);
        dropwizardResourceConfig.register(ClustersResource.class);

        final JerseyTest jerseyTest = new JerseyTest() {
            @Override
            protected Application configure() {
                return dropwizardResourceConfig;
            }
        };

        jerseyTest.setUp();
        jerseyTest.tearDown();
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
            return Arrays.asList("first_asset", "second_asset");
        }

        @DELETE
        @Path("{id}")
        public void delete(@PathParam("id") String id) {
            // stub implementation
        }

        @PUT
        @Path("{id}")
        public void update(@PathParam("id") String id, String asset) {
            // stub implementation
        }
    }

    @Path("distributions")
    public static class DistributionResource {

        @POST
        @Path("{assetId}/clusters/{code}/start")
        public void start(@PathParam("assetId") String assetId,
                                      @PathParam("code") String code) {
            // stub implementation
        }

        @POST
        @Path("{assetId}/clusters/{code}/complete")
        public void complete(@PathParam("assetId") String assetId,
                                         @PathParam("code") String code) {
            // stub implementation
        }

        @POST
        @Path("{assetId}/clusters/{code}/abort")
        public void abort(@PathParam("assetId") String assetId,
                                      @PathParam("code") String code) {
            // stub implementation
        }

        @POST
        @Path("{assetId}/clusters/{code}/delete")
        public void delete(@PathParam("assetId") String assetId,
                                       @PathParam("code") String code) {
            // stub implementation
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
            return Arrays.asList("first_cluster", "second_cluster", "third_cluster");
        }

        @DELETE
        @Path("{code}")
        public void delete(@PathParam("code") String code) {
            // stub implementation
        }
    }
}
