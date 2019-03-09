.. _upgrade-notes-dropwizard-0_8_x:

##################################
Upgrade Notes for Dropwizard 0.8.x
##################################

First
=====

Check out `Migration discussion 0.7.1 to 0.8.0 <https://groups.google.com/forum/#!topic/dropwizard-dev/VInOW_ebiAc>`__
at the ``dropwizard-dev`` mailing list.

Migration of Apache Commons Lang
================================

The classes were moved to a new package. You have to update the corresponding imports:

| search for: ``org.apache.commons.lang.``
| replace with: ``org.apache.commons.lang3.``

Use assertions from AssertJ
===========================

Instead of the FEST assertions you should use the AssertJ assertions:

| search for: ``org.fest.assertions.api.Assertions.``
| replace with: ``org.assertj.core.api.Assertions.``

Migration of custom URL pattern
===============================

If you set a custom URL pattern in your application ``run`` method you should move the definition to your configuration file:

Remove from Java code (example):

.. code-block:: java

   environment.jersey().setUrlPattern("/api/*");

Add to configuration file (example):

.. code-block:: yaml

   server:
     rootPath: '/api/*'

Migration of Jersey
===================

This is not a simple *search and replace* migration, so I show you a few
examples of often used code snippets for integration testing:

Dropwizard Class Rule
---------------------

The class rule was not modified. It is shown here because it is used in
the examples below.

.. code-block:: java

   @ClassRule
   public static final DropwizardAppRule<SportChefConfiguration> RULE = 
                       new DropwizardAppRule<>(App.class, "config.yaml");

Executing a GET request
-----------------------

.. code-block:: java

   final WebTarget target = ClientBuilder.newClient().target(
           String.format("http://localhost:%d/api/user/1", RULE.getLocalPort()));

   final Response response = target
           .request(MediaType.APPLICATION_JSON_TYPE)
           .accept(MediaType.APPLICATION_JSON_TYPE)
           .get();

   assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

   final User user = response.readEntity(User.class);
   assertThat(user.getId()).isEqualTo(1L);
   assertThat(user.getFirstName()).isEqualTo("John");
   assertThat(user.getLastName()).isEqualTo("Doe");

Executing a POST request
------------------------

.. code-block:: java

   final WebTarget target = ClientBuilder.newClient().target(
           String.format("http://localhost:%d/api/user", RULE.getLocalPort()));

   final User user = new User(0L, "John", "Doe");

   final Response response = target
           .request(MediaType.APPLICATION_JSON_TYPE)
           .accept(MediaType.APPLICATION_JSON_TYPE)
           .post(Entity.json(user));

   assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

   final URI location = response.getLocation();
   assertThat(location).isNotNull();

   final String path = location.getPath();
   final long newId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
   assertThat(newId).isGreaterThan(0);

Executing a empty PUT request
-----------------------------

Jersey 2 does not by default allow empty PUT or DELETE requests.
If you want to enable this, you have to add a configuration parameter

.. code-block:: java

   Client client = ClientBuilder.newClient();
   client.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
   WebTarget target = client.target(
           String.format("http://localhost:%d/api/user", RULE.getLocalPort()));

   Response response = target
           .request()
           .put(null);

   assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

Request/response filters
------------------------

If you previously used jersey container filters in your Dropwizard app,
``getContainerRequestFilters()`` will now fail to resolve:

.. code-block:: java

   env.jersey()
   .getResourceConfig()
   .getContainerRequestFilters()
   .add(new AuthorizedFilter());

You might need to rewrite the filter to JAX-RS 2.0 and then you may use
the one and only ``.register()`` instead.

My filters used imports from ``jersey.spi.container`` and needed to be rewritten for Jersey 2.x.
See also: `Jersey 1.x to 2.x migration guide <https://jersey.github.io/documentation/2.16/user-guide.html#mig-1.x>`_.

.. code-block:: java

   env.jersey().register(new AuthorizationFilter());
