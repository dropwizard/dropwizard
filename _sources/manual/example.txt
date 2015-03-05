.. _man-example:

################################
Dropwizard Example, Step by Step
################################

.. highlight:: text

.. rubric:: The ``dropwizard-example`` module provides you with a working example
            of a dropwizard app


* Open a terminal
* Make sure you have maven installed
* Make sure java home points at JDK 7
* Make sure you have curl
* mvn dependency:resolve
* mvn clean compile install
* mvn eclipse:eclipse -DdownloadSources=true
* From eclipse, File --> Import --> Existing Project into workspace
* ``java -jar ~/git/dropwizard/dropwizard-example/target/dropwizard-example-0.8.0-SNAPSHOT.jar db migrate example.yml``
* The above ran the liquibase migration in /src/main/resources/migrations.xml, creating the table schema
* You can now start the app in your IDE by running ``java -jar ~/git/dropwizard/dropwizard-example/target/dropwizard-example-0.8.0-SNAPSHOT.jar db migrate example.yml``
* Alternatively you can run this file in your IDE: ``com.example.helloworld.HelloWorldApplication server example.yml``
* Insert a new person: ``curl -H "Content-Type: application/json" -X POST -d '{"fullName":"Coda Hale", "jobTitle" : "Chief Wizard" }' http://localhost:8080/people``
* Retrieve that person: ``curl http://localhost:8080/people/1``
* View the freemarker template: ``curl http://localhost:8080/people/1/view_freemarker``
* View the mustache template: ``curl http://localhost:8080/people/1/view_mustache``

