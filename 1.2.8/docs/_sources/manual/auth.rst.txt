.. _man-auth:

#########################
Dropwizard Authentication
#########################

.. rubric:: The ``dropwizard-auth`` client provides authentication using either HTTP Basic
            Authentication or OAuth2 bearer tokens.

.. _man-auth-authenticators:

Authenticators
==============

An authenticator is a strategy class which, given a set of client-provided credentials, possibly
returns a principal (i.e., the person or entity on behalf of whom your service will do something).

Authenticators implement the ``Authenticator<C, P extends Principal>`` interface, which has a single method:

.. code-block:: java

    public class ExampleAuthenticator implements Authenticator<BasicCredentials, User> {
        @Override
        public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
            if ("secret".equals(credentials.getPassword())) {
                return Optional.of(new User(credentials.getUsername()));
            }
            return Optional.empty();
        }
    }

This authenticator takes :ref:`basic auth credentials <man-auth-basic>` and if the client-provided
password is ``secret``, authenticates the client as a ``User`` with the client-provided username.

If the password doesn't match, an absent ``Optional`` is returned instead, indicating that the
credentials are invalid.

.. warning:: It's important for authentication services not to provide too much information in their
             errors. The fact that a username or email has an account may be meaningful to an
             attacker, so the ``Authenticator`` interface doesn't allow you to distinguish between
             a bad username and a bad password. You should only throw an ``AuthenticationException``
             if the authenticator is **unable** to check the credentials (e.g., your database is
             down).

.. _man-auth-authenticators-caching:

Caching
-------

Because the backing data stores for authenticators may not handle high throughput (an RDBMS or LDAP
server, for example), Dropwizard provides a decorator class which provides caching:

.. code-block:: java

    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator();
    CachingAuthenticator<BasicCredentials, User> cachingAuthenticator = new CachingAuthenticator<>(
                               metricRegistry, simpleAuthenticator,
                               config.getAuthenticationCachePolicy());

Dropwizard can parse Guava's ``CacheBuilderSpec`` from the configuration policy, allowing your
configuration file to look like this:

.. code-block:: yaml

    authenticationCachePolicy: maximumSize=10000, expireAfterAccess=10m

This caches up to 10,000 principals with an LRU policy, evicting stale entries after 10 minutes.

.. _man-auth-authorizer:

Authorizer
==========

An authorizer is a strategy class which, given a principal and a role, decides if access is granted to the
principal.

The authorizer implements the ``Authorizer<P extends Principal>`` interface, which has a single method:

.. code-block:: java

    public class ExampleAuthorizer implements Authorizer<User> {
        @Override
        public boolean authorize(User user, String role) {
            return user.getName().equals("good-guy") && role.equals("ADMIN");
        }
    }

.. _man-auth-basic:

Basic Authentication
====================

The ``AuthDynamicFeature`` with the ``BasicCredentialAuthFilter`` and ``RolesAllowedDynamicFeature``
enables HTTP Basic authentication and authorization; requires an authenticator which
takes instances of ``BasicCredentials``. If you don't use authorization, then ``RolesAllowedDynamicFeature``
is not required.

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(new ExampleAuthenticator())
                    .setAuthorizer(new ExampleAuthorizer())
                    .setRealm("SUPER SECRET STUFF")
                    .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }

.. _man-auth-oauth2:

OAuth2
======

The ``AuthDynamicFeature`` with ``OAuthCredentialAuthFilter`` and ``RolesAllowedDynamicFeature``
enables OAuth2 bearer-token authentication and authorization; requires an authenticator which
takes instances of ``String``. If you don't use authorization, then ``RolesAllowedDynamicFeature``
is not required.

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        environment.jersey().register(new AuthDynamicFeature(
            new OAuthCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new ExampleOAuthAuthenticator())
                .setAuthorizer(new ExampleAuthorizer())
                .setPrefix("Bearer")
                .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }

.. _man-auth-chained:

Chained Factories
=================

The ``ChainedAuthFilter`` enables usage of various authentication factories at the same time.

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        AuthFilter basicCredentialAuthFilter = new BasicCredentialAuthFilter.Builder<>()
                .setAuthenticator(new ExampleBasicAuthenticator())
                .setAuthorizer(new ExampleAuthorizer())
                .setPrefix("Basic")
                .buildAuthFilter();

        AuthFilter oauthCredentialAuthFilter = new OAuthCredentialAuthFilter.Builder<>()
                .setAuthenticator(new ExampleOAuthAuthenticator())
                .setAuthorizer(new ExampleAuthorizer())
                .setPrefix("Bearer")
                .buildAuthFilter();

        List<AuthFilter> filters = Lists.newArrayList(basicCredentialAuthFilter, oauthCredentialAuthFilter);
        environment.jersey().register(new AuthDynamicFeature(new ChainedAuthFilter(filters)));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        //If you want to use @Auth to inject a custom Principal type into your resource
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }

For this to work properly, all chained factories must produce the same type of principal, here ``User``.


.. _man-auth-resources:

Protecting Resources
====================

There are two ways to protect a resource.  You can mark your resource method with one of the following annotations:

* ``@PermitAll``. All authenticated users will have access to the method.
* ``@RolesAllowed``. Access will be granted to the users with the specified roles.
* ``@DenyAll``. No access will be granted to anyone.

.. note::
    You can use ``@RolesAllowed``, ``@PermitAll`` on the class level. Method annotations take precedence over the class ones.

Alternatively, you can annotate the parameter representing your principal with ``@Auth``. Note you must register a
jersey provider to make this work.

.. code-block:: java

    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

    @RolesAllowed("ADMIN")
    @GET
    public SecretPlan getSecretPlan(@Auth User user) {
        return dao.findPlanForUser(user);
    }

You can also access the Principal by adding a parameter to your method ``@Context SecurityContext context``. Note this
will not automatically register the servlet filter which performs authentication. You will still need to add one of
``@PermitAll``, ``@RolesAllowed``, or ``@DenyAll``. This is not the case with ``@Auth``. When that is present, the auth
filter is automatically registered to facilitate users upgrading from older versions of Dropwizard

.. code-block:: java

    @RolesAllowed("ADMIN")
    @GET
    public SecretPlan getSecretPlan(@Context SecurityContext context) {
        User userPrincipal = (User) context.getUserPrincipal();
        return dao.findPlanForUser(user);
    }

If there are no provided credentials for the request, or if the credentials are invalid, the
provider will return a scheme-appropriate ``401 Unauthorized`` response without calling your
resource method.

Optional protection
-------------------

Resource methods can be _optionally_ protected by representing the
principal as an ``Optional``. In such cases, the ``Optional`` resource
method argument will be populated with the principal, if
present. Otherwise, the argument will be ``Optional.empty``.

For instance, say you have an endpoint that should display a logged-in
user's name, but return an anonymous reply for unauthenticated
requests. You need to implement a custom filter which injects a
security context containing the principal if it exists, without
performing authentication.

.. code-block:: java

    @GET
    public String getGreeting(@Auth Optional<User> userOpt) {
        if (userOpt.isPresent()) {
            return "Hello, " + userOpt.get().getName() + "!";
        } else {
            return "Greetings, anonymous visitor!"
        }
    }

For optionally-protected resources, requests with invalid auth will be
treated the same as those with no provided auth credentials. That is
to say, requests that _fail_ to meet an authenticator or authorizer's
requirements result in an empty principal being passed to the resource
method.

Testing Protected Resources
===========================

Add this dependency into your ``pom.xml`` file:

.. code-block:: xml

    <dependencies>
      <dependency>
        <groupId>io.dropwizard</groupId>
        <artifactId>dropwizard-testing</artifactId>
        <version>${dropwizard.version}</version>
      </dependency>
      <dependency>
        <groupId>org.glassfish.jersey.test-framework.providers</groupId>
        <artifactId>jersey-test-framework-provider-grizzly2</artifactId>
        <version>${jersey.version}</version>
        <exclusions>
          <exclusion>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
          </exclusion>
          <exclusion>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
          </exclusion>
        </exclusions>
      </dependency>
    </dependencies>

When you build your ``ResourceTestRule``, add the ``GrizzlyWebTestContainerFactory`` line.

.. code-block:: java

    @Rule
    public ResourceTestRule rule = ResourceTestRule
            .builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(new MyOAuthAuthenticator())
                    .setAuthorizer(new MyAuthorizer())
                    .setRealm("SUPER SECRET STUFF")
                    .setPrefix("Bearer")
                    .buildAuthFilter()))
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .addResource(new ProtectedResource())
            .build();


In this example, we are testing the oauth authentication, so we need to set the header manually.

.. code-block:: java

    @Test
    public void testProtected() throws Exception {
        final Response response = rule.target("/protected")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer TOKEN")
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

Multiple Principals and Authenticators
======================================

In some cases you may want to use different authenticators/authentication schemes for different
resources. For example you may want Basic authentication for one resource and OAuth
for another resource, at the same time using a different `Principal` for each
authentication scheme.

For this use case, there is the ``PolymorphicAuthDynamicFeature`` and the
``PolymorphicAuthValueFactoryProvider``. With these two components, we can use different
combinations of authentication schemes/authenticators/authorizers/principals. To use this
feature, we need to do a few things:

* Register the ``PolymorphicAuthDynamicFeature`` with a map that maps principal types to
  authentication filters.

* Register the ``PolymorphicAuthValueFactoryProvider`` with a set of principal classes
  that you will be using.

* Annotate your resource method ``Principal`` parameters with ``@Auth``.

As an example, the following code configures both OAuth and Basic authentication, using
a different principal for each.

.. code-block:: java

    final AuthFilter<BasicCredentials, BasicPrincipal> basicFilter
            = new BasicCredentialAuthFilter.Builder<BasicPrincipal>()
                    .setAuthenticator(new ExampleAuthenticator())
                    .setRealm("SUPER SECRET STUFF")
                    .buildAuthFilter());
    final AuthFilter<String, OAuthPrincipal> oauthFilter
            = new OAuthCredentialAuthFilter.Builder<OAuthPrincipal>()
                    .setAuthenticator(new ExampleOAuthAuthenticator())
                    .setPrefix("Bearer")
                    .buildAuthFilter());

    final PolymorphicAuthDynamicFeature feature = new PolymorphicAuthDynamicFeature<>(
        ImmutableMap.of(
            BasicPrincipal.class, basicFilter,
            OAuthPrincipal.class, oauthFilter));
    final AbstractBinder binder = new PolymorphicAuthValueFactoryProvider.Binder<>(
        ImmutableSet.of(BasicPrincipal.class, OAuthPrincipal.class));

    environment.jersey().register(feature);
    environment.jersey().register(binder);

Now we are able to do something like the following

.. code-block:: java

    @GET
    public Response basicAuthResource(@Auth BasicPrincipal principal) {}

    @GET
    public Response oauthResource(@Auth OAuthPrincipal principal) {}

The first resource method will use Basic authentication while the second one will use OAuth.

Note that with the above example, only *authentication* is configured. If you also want
*authorization*, the following steps will need to be taken.

* Register the ``RolesAllowedDynamicFeature`` with the application.

* Make sure you add ``Authorizers`` when you build your ``AuthFilters``.

* Annotate the resource *method* with the authorization annotation. Unlike the note earlier in
  this document that says authorization annotations are allowed on classes, with this
  poly feature, currently that is not supported. The annotation MUST go on the resource *method*

So continuing with the previous example you should add the following configurations

.. code-block:: java

    ... = new BasicCredentialAuthFilter.Builder<BasicPrincipal>()
            .setAuthorizer(new ExampleAuthorizer())..  // set authorizer

    ... = new OAuthCredentialAuthFilter.Builder<OAuthPrincipal>()
            .setAuthorizer(new ExampleAuthorizer())..  // set authorizer

    environment.jersey().register(RolesAllowedDynamicFeature.class);

Now we can do

.. code-block:: java

    @GET
    @RolesAllowed({ "ADMIN" })
    public Response baseAuthResource(@Auth BasicPrincipal principal) {}

    @GET
    @RolesAllowed({ "ADMIN" })
    public Response oauthResource(@Auth OAuthPrincipal principal) {}

.. note::
    The polymorphic auth feature *SHOULD NOT* be used with any other ``AuthDynamicFeature``. Doing so may have undesired effects.
