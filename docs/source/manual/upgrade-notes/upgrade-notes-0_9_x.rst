.. _upgrade-notes-dropwizard-0_9_x:

##################################
Upgrade Notes for Dropwizard 0.9.x
##################################

Migrating Auth
==============

1. Any custom types representing a user need to implement the
   ``Principal`` interface

2. In your ``Application#run`` add

   .. code-block:: java

      environment.jersey().register(RolesAllowedDynamicFeature.class);

3. Create an Authorizer

   .. code-block:: java

      public class ExampleAuthorizer implements Authorizer<User> {
        @Override
        public boolean authorize(User user, String role) {
          return user.getName().equals("good-guy") && role.equals("ADMIN");
        }
      }

4. Create an ``AuthFilter`` using your ``Authenticator`` and ``Authorizer``

   .. code-block:: java

      final BasicCredentialAuthFilter<User> userBasicCredentialAuthFilter =
              new BasicCredentialAuthFilter.Builder<User>()
                      .setAuthenticator(new ExampleAuthenticator())
                      .setRealm("SUPER SECRET STUFF")
                      .setAuthorizer(new ExampleAuthorizer())
                      .buildAuthFilter();

5. Register ``AuthDynamicFeature`` with your ``AuthFilter``

   .. code-block:: java

      environment.jersey().register(new AuthDynamicFeature(userBasicCredentialAuthFilter));

6. Register the ``AuthValueFactoryProvider.Binder`` so with your custom user type if you have one

   .. code-block:: java

      environment.jersey().register(new AuthValueFactoryProvider.Binder(User.class));

7. Annotate resources methods that already have ``@Auth`` with ``@RolesAllowed("admin")`` where admin is a role

   .. code-block:: shell

      $ curl 'testUser:secret@localhost:8080/protected'
      Hey there, testUser. You know the secret!

UnwrapValidatedValue Changes
============================

With the upgrade to Hibernate Validator 5.2.1.Final, the behavior of ``@UnwrapValidatedValue`` has slightly changed.
In some situations, the annotation is now unnecessary.
However, when inference is not possible and is ambiguous where the constraint annotation applies, a runtime exception is thrown.
This is only a problem when dealing with constraints that can apply to both the wrapper and inner type like ``@NotNull``.
The fix is to explicitly set ``false`` or ``true`` for ``@UnwrapValidatedValue``

For instance if you previously had code like:

.. code-block:: java

   @GET
   public String heads(@QueryParam("cheese") @NotNull IntParam secretSauce) {

Where ``@NotNull`` is meant to apply to wrapper type of ``IntParam`` and not the inner type of
``Integer`` (as ``IntParam`` will never yield a ``null`` integer).
Hibernate Validator doesn't know this, but it does know that ``@NotNull`` can be applied to both ``IntParam`` and ``Integer``,
so in Dropwizard 0.9.x the previous code will now fail and must be changed to

.. code-block:: java

   @GET
   public String heads(@QueryParam("cheese") @NotNull @UnwrapValidatedValue(false) IntParam secretSauce) {

For more information on the behavior changes, see `accompanying table for automatic value unwrapping <https://hibernate.atlassian.net/browse/HV-925>`__

Logging bootstrap
=================

If you configured console logging in your tests with a utility method shipped with Dropwizard,
you should replace calls of ``LoggingFactory.bootstrap`` to ``BootstrapLogging.bootstrap``.
