# Dropwizard Codegenerator Maven Plugin

Are you sick of always having to implement and maintain the client-side services for reaching your Dropwizard Application.
With the Dropwizard Codegenerator Maven Plugin you are able to automatically generate the client-side code to reach your API.

For now the plugin generates java code as a proof of concept - see [example of usage](https://github.com/jansoren/akka-persistence-java-example), but feel free to create pull requests and extend the plugin to generate for example Ajax, AngularJS or ReactJS code.

## Example of generating Java code

- `generatedCodeFolder`: This is the folder where you want your generated code
- `generatedCodePackage`: This is the package of your generated class files
- `rootHost`: This is the host of your running application

Add the plugin to your `pom.xml` file and run `mvn clean install`. The plugin will scan your source code and generate code out of your resource classes.
Enjoy :-)

```
<plugin>
    <groupId>io.dropwizard</groupId>
    <artifactId>dropwizard-codegen-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <generatedCodeFolder>../server-qtest/src/main/java</generatedCodeFolder>
        <generatedCodePackage>com.example.qtest.services</generatedCodePackage>
        <rootHost>http://localhost:8080</rootHost>
    </configuration>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>codegen</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Example of generating ReactJS code

Not implemented yet
