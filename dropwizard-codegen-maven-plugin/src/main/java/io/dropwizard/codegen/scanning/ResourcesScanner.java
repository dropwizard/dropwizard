package io.dropwizard.codegen.scanning;

import com.google.common.collect.Lists;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.ws.rs.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResourcesScanner {

    private Log log;
    private MavenProject mavenProject;

    public ResourcesScanner(Log log, MavenProject mavenProject) {
        this.log = log;
        this.mavenProject = mavenProject;
    }

    public List<ScannedClass> scan(String packageToScan, String rootHost) throws MojoExecutionException {
        ConfigurationBuilder configuration = createConfiguration();

        Reflections reflections = new Reflections(configuration);

        List<ScannedClass> scannedClasses = new ArrayList<>();
        for(Class<?> scannedResourceClass : reflections.getTypesAnnotatedWith(javax.ws.rs.Path.class)) {
            scannedClasses.add(createScannedClass(rootHost, scannedResourceClass));
        }
        return scannedClasses;
    }

    private ConfigurationBuilder createConfiguration() {
        if(mavenProject != null) {
            URL[] urls = {getMavenProjectUrl()};
            return new ConfigurationBuilder()
                .setUrls(urls)
                .addClassLoader(mavenClassLoader());
        } else {
            URL[] urls = {getSrcFolderUrl()};
            return new ConfigurationBuilder()
                .setUrls(urls);
        }
    }

    private URL getSrcFolderUrl() {
        try {
            return new File("").toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    private URL getMavenProjectUrl() {
        try {
            return new File(resolveOutputDirectory(mavenProject) + '/').toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private URLClassLoader mavenClassLoader() {
        try {
            List<String> elements = Lists.newArrayList(mavenProject.getRuntimeClasspathElements());
            elements.addAll(mavenProject.getTestClasspathElements());
            URL[] urls = new URL[elements.size()];
            int i = 0;
            for (String element : elements) {
                URL url = new File(element).toURI().toURL();
                urls[i++] = url;
                log.info("Found url " + i + ": " + url);
            }
            return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String resolveOutputDirectory(MavenProject mavenProject) {
        return mavenProject.getBuild().getOutputDirectory();
    }

    private static ScannedClass createScannedClass(String rootHost, Class<?> scannedResourceClass) {
        ScannedClass scannedClass = new ScannedClass();
        scannedClass.setName(scannedResourceClass.getSimpleName());
        scannedClass.setScannedMethods(scanMethods(rootHost, scannedResourceClass));
        return scannedClass;
    }

    private static List<ScannedMethod> scanMethods(String rootHost, Class<?> scannedResourceClass) {
        List<ScannedMethod> scannedMethods = new ArrayList<>();
        for(Method scannedMethod : scannedResourceClass.getDeclaredMethods()) {
            scannedMethods.add(createScannedMethod(scannedResourceClass, scannedMethod, rootHost));
        }
        return scannedMethods;
    }

    private static ScannedMethod createScannedMethod(Class<?> resourceClass, Method method, String rootHost) {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName(method.getName());
        scannedMethod.setMethod(getHttpMethod(method));
        scannedMethod.setUrl(rootHost + getClassPath(resourceClass) + getMethodPath(method));
        scannedMethod.setClassToReturn(method.getReturnType());
        scannedMethod.setClassToPost(getClassToPost(method));
        return scannedMethod;
    }

    private static Class<?> getClassToPost(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if(parameterTypes.length > 0) {
            return parameterTypes[0];
        }
        return null;
    }

    private static String getMethodPath(Method method) {
        Path[] annotationsByType = method.getAnnotationsByType(Path.class);
        if(annotationsByType != null && annotationsByType.length > 0) {
            Path annotation1 = annotationsByType[0];
            return annotation1.value();
        }
        return "";
    }

    private static String getClassPath(Class<?> resourceClass) {
        Path[] annotationsByType = resourceClass.getAnnotationsByType(Path.class);
        if(annotationsByType != null && annotationsByType.length > 0) {
            Path annotation = annotationsByType[0];
            return annotation.value();
        }
        return "";
    }

    private static String getHttpMethod(Method method) {
        if(isAnnotation(method, POST.class)){
            return HttpMethod.POST;
        }else if(isAnnotation(method, GET.class)){
            return HttpMethod.GET;
        }else if(isAnnotation(method, PUT.class)){
            return HttpMethod.PUT;
        }else if(isAnnotation(method, DELETE.class)){
            return HttpMethod.DELETE;
        }
        return null;
    }

    private static boolean isAnnotation(Method method, Class clz) {
        return method.getAnnotationsByType(clz).length > 0;
    }
}
