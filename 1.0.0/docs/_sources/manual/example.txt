.. _man-example:

################################
Dropwizard Example, Step by Step
################################

.. highlight:: text

.. rubric:: The ``dropwizard-example`` module provides you with a working Dropwizard Example Application.

* Preconditions

  * Make sure you have Maven installed
  * Make sure ``JAVA_HOME`` points at JDK 7
  * Make sure you have ``curl``

* Preparations to start the Dropwizard Example Application

  * Open a terminal / cmd
  * Navigate to the project folder of the Dropwizard Example Application
  * ``mvn clean install``
  * ``java -jar target/dropwizard-example-0.9.0.jar db migrate example.yml``
  * The statement above ran the liquibase migration in ``/src/main/resources/migrations.xml``, creating the table schema

* Starting the Dropwizard Example Application

  * You can now start the Dropwizard Example Application by running ``java -jar target/dropwizard-example-0.9.0.jar server example.yml``
  * Alternatively, you can run the Dropwizard Example Application in your IDE: ``com.example.helloworld.HelloWorldApplication server example.yml``

* Working with the Dropwizard Example Application

  * Insert a new person: ``curl -H "Content-Type: application/json" -X POST -d '{"fullName":"John Doe", "jobTitle" : "Chief Wizard" }' http://localhost:8080/people``
  * Retrieve that person: ``curl http://localhost:8080/people/1``
  * View that person in a freemarker template: curl or open in a browser ``http://localhost:8080/people/1/view_freemarker``
  * View that person in a mustache template: curl or open in a browser ``http://localhost:8080/people/1/view_mustache``
