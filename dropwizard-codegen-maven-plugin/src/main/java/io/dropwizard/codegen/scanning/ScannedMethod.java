package io.dropwizard.codegen.scanning;

public class ScannedMethod {

    private String name;
    private String method;
    private String url;
    private Class classToReturn;
    private Class classToPost;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Class getClassToReturn() {
        return classToReturn;
    }

    public void setClassToReturn(Class classToReturn) {
        this.classToReturn = classToReturn;
    }

    public Class getClassToPost() {
        return classToPost;
    }

    public void setClassToPost(Class classToPost) {
        this.classToPost = classToPost;
    }

    @Override
    public String toString() {
        return "ScannedMethod{" +
            "name='" + name + '\'' +
            ", method='" + method + '\'' +
            ", url='" + url + '\'' +
            '}';
    }
}
