package com.yammer.dropwizard.swagger;

import com.sun.jersey.api.core.ResourceConfig;
import com.wordnik.swagger.jaxrs.ApiListingResourceJSON;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/resources.json")
@Produces(MediaType.APPLICATION_JSON)
public class ApiListingResource extends ApiListingResourceJSON {

    @GET
    @Override
    public Response getAllApis(@Context ServletConfig sc,
                               @Context ResourceConfig rc,
                               @Context HttpHeaders headers,
                               @Context UriInfo uriInfo) {
        return super.getAllApis(sc, new ResourceConfigDelegate(rc), headers, uriInfo);
    }

    private static class ResourceConfigDelegate extends ResourceConfig {

        private final ResourceConfig resourceConfig;

        private ResourceConfigDelegate(ResourceConfig resourceConfig) {
            this.resourceConfig = resourceConfig;
        }

        /**
         * Merge root resource classes with the singleton classes
         * as an workaround for a limitation (or bug?) in Swagger
         */
        @Override
        public Set<Class<?>> getRootResourceClasses() {
            Set<Class<?>> result = resourceConfig.getRootResourceClasses();
            for (Object singleton : resourceConfig.getRootResourceSingletons()) {
                result.add(singleton.getClass());
            }
            return result;
        }

        @Override
        public Map<String, Boolean> getFeatures() {
            return resourceConfig.getFeatures();
        }

        @Override
        public boolean getFeature(String s) {
            return resourceConfig.getFeature(s);
        }

        @Override
        public Map<String, Object> getProperties() {
            return resourceConfig.getProperties();
        }

        @Override
        public Object getProperty(String s) {
            return resourceConfig.getProperty(s);
        }

        @Override
        public Map<String, MediaType> getMediaTypeMappings() {
            return resourceConfig.getMediaTypeMappings();
        }

        @Override
        public Map<String, String> getLanguageMappings() {
            return resourceConfig.getLanguageMappings();
        }

        @Override
        public Map<String, Object> getExplicitRootResources() {
            return resourceConfig.getExplicitRootResources();
        }

        @Override
        public void validate() {
            resourceConfig.validate();
        }

        @Override
        public Set<Class<?>> getProviderClasses() {
            return resourceConfig.getProviderClasses();
        }

        @Override
        public Set<Object> getRootResourceSingletons() {
            return resourceConfig.getRootResourceSingletons();
        }

        @Override
        public Set<Object> getProviderSingletons() {
            return resourceConfig.getProviderSingletons();
        }

        public static boolean isRootResourceClass(Class<?> c) {
            return ResourceConfig.isRootResourceClass(c);
        }

        public static boolean isProviderClass(Class<?> c) {
            return ResourceConfig.isProviderClass(c);
        }

        @Override
        public List getContainerRequestFilters() {
            return resourceConfig.getContainerRequestFilters();
        }

        @Override
        public List getContainerResponseFilters() {
            return resourceConfig.getContainerResponseFilters();
        }

        @Override
        public List getResourceFilterFactories() {
            return resourceConfig.getResourceFilterFactories();
        }

        @Override
        public void setPropertiesAndFeatures(Map<String, Object> entries) {
            resourceConfig.setPropertiesAndFeatures(entries);
        }

        @Override
        public void add(Application app) {
            resourceConfig.add(app);
        }

        public static String[] getElements(String[] elements) {
            return ResourceConfig.getElements(elements);
        }

        public static String[] getElements(String[] elements, String delimiters) {
            return ResourceConfig.getElements(elements, delimiters);
        }

        @Override
        public Set<Class<?>> getClasses() {
            return resourceConfig.getClasses();
        }

        @Override
        public Set<Object> getSingletons() {
            return resourceConfig.getSingletons();
        }
    }
}
