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

    public class SimpleAuthenticator implements Authenticator<BasicCredentials, User> {
        @Override
        public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
            if ("secret".equals(credentials.getPassword())) {
                return Optional.of(new User(credentials.getUsername()));
            }
            return Optional.absent();
        }
    }

This authenticator takes :ref:`basic auth credentials <man-auth-basic>` and if the client-provided
password is ``secret``, authenticates the client as a ``User`` with the client-provided username.

If the password doesn't match, an absent ``Optional`` is returned instead, indicating that the
credentials are invalid.

.. warning:: It's important for authentication services to not provide too much information in their
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

.. _man-auth-basic:

Basic Authentication
====================

The ``AuthDynamicFeature`` with the ``BasicCredentialAuthHandler`` and ``RolesAllowedDynamicFeature``
enables HTTP Basic authentication and authorization; requires an authenticator which
takes instances of ``BasicCredentials``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {

        final Function<AuthFilter.Tuple, SecurityContext> securityContextFunction =
            new Function<AuthFilter.Tuple, SecurityContext>() {
                @Override
                public SecurityContext apply(final AuthFilter.Tuple input) {
                    return new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return input.getPrincipal();
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return getUserPrincipal() != null
                                    && validUser.equals(getUserPrincipal().getName())
                                    && validRole.equals(role);
                        }

                        @Override
                        public boolean isSecure() {
                            return input.getContainerRequestContext().getSecurityContext().isSecure();
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return SecurityContext.BASIC_AUTH;
                        }
                    }
                }
            }
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthHandler.Builder<User, ExampleAuthenticator>()
                    .setAuthenticator(new ExampleAuthenticator())
                    .setRealm("SUPER SECRET STUFF")
                    .setSecurityContextFunction(securityContextFunction);
                    .buildAuthHandler()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }

.. _man-auth-oauth2:

OAuth2
======

The ``AuthDynamicFeature`` with ``OAuthCredentialAuthHandler`` and ``RolesAllowedDynamicFeature``
enables OAuth2 bearer-token authentication and authorization;
requires an authenticator which takes instances of ``String``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        final Authenticator<String, Principal> authenticator = new Authenticator<String, Principal>() {
            @Override
            public Optional<Principal> authenticate(String credentials) throws AuthenticationException {
                if ("good-guy".equals(credentials)) {
                    return Optional.<Principal>of(new PrincipalImpl("good-guy"));
                }

                if ("bad-guy".equals(credentials)) {
                    throw new AuthenticationException("CRAP");
                }

                return Optional.absent();
            }
        };

        final Function<AuthFilter.Tuple, SecurityContext> securityContextFunction =
            new Function<AuthFilter.Tuple, SecurityContext>() {
                @Override
                public SecurityContext apply(final AuthFilter.Tuple input) {
                    return new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return input.getPrincipal();
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return getUserPrincipal() != null
                                    && validUser.equals(getUserPrincipal().getName())
                                    && validRole.equals(role);
                        }

                        @Override
                        public boolean isSecure() {
                            return input.getContainerRequestContext().getSecurityContext().isSecure();
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return SecurityContext.BASIC_AUTH;
                        }
                    }
                }
            }

        environment.jersey().register(new AuthDynamicFeature(
            new OAuthCredentialAuthHandler.Builder<>()
                .setAuthenticator(authenticator)
                .setSecurityContextFunction(securityContextFunction);
                .setPrefix("Custom")
                .buildAuthHandler()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }

.. _man-auth-chained:

Chained Factories
=================

The ``ChainedAuthHandler`` enables usage of various authentication factories at the same time.

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        final Authenticator<BasicCredentials, Principal> basicAuthenticator = new Authenticator<BasicCredentials, Principal>() {
            @Override
            public Optional<Principal> authenticate(BasicCredentials credentials) throws AuthenticationException {
                if ("good-guy".equals(credentials.getUsername()) &&
                        "secret".equals(credentials.getPassword())) {
                    return Optional.<Principal>of(new PrincipalImpl("good-guy"));
                }
                if ("bad-guy".equals(credentials.getUsername())) {
                    throw new AuthenticationException("CRAP");
                }
                return Optional.absent();
            }
        };

        final Authenticator<String, Principal> oauthAuthenticator = new Authenticator<String, Principal>() {
            @Override
            public Optional<Principal> authenticate(String credentials) throws AuthenticationException {
                if ("A12B3C4D".equals(credentials)) {
                    return Optional.<Principal>of(new PrincipalImpl("good-guy"));
                }
                if ("bad-guy".equals(credentials)) {
                    throw new AuthenticationException("CRAP");
                }
                return Optional.absent();
            }
        };

        final Function<AuthFilter.Tuple, SecurityContext> securityContextFunction =
            new Function<AuthFilter.Tuple, SecurityContext>() {
                @Override
                public SecurityContext apply(final AuthFilter.Tuple input) {
                    return new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return input.getPrincipal();
                        }

                        @Override
                        public boolean isUserInRole(String role) {
                            return getUserPrincipal() != null
                                    && validUser.equals(getUserPrincipal().getName())
                                    && validRole.equals(role);
                        }

                        @Override
                        public boolean isSecure() {
                            return input.getContainerRequestContext().getSecurityContext().isSecure();
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return SecurityContext.BASIC_AUTH;
                        }
                    }
                }
            }

        AuthHandler basicCredentialAuthHandler = new BasicCredentialAuthHandler.Builder()
                .setSecurityContextFunction(securityContextFunction);
                .setAuthenticator(basicAuthenticator)
                .buildAuthHandler();

        AuthHandler oauthCredentialAuthHandler = new OAuthCredentialAuthHandler.Builder()
                .setSecurityContextFunction(securityContextFunction);
                .setAuthenticator(oauthAuthenticator)
                .setPrefix("Bearer")
                .buildAuthHandler();

        List handlers = Lists.newArrayList(basicCredentialAuthHandler, oauthCredentialAuthHandler);
        environment.jersey().register(new AuthDynamicFeature(new ChainedAuthHandler(handlers)));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }

For this to work properly, all chained factories must produce the same type of principal, here ``User``.


.. _man-auth-resources:

Protecting Resources
====================

To protect a resource, simply include the ``@RolesAllowed`` annotation on your resource method.
If you need access to the Principal, you need to add a parameter to your method ``@Context SecurityContext context``

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

If you have a resource which is optionally protected (e.g., you want to display a logged-in user's
name but not require login), set the ``required`` attribute of the annotation to ``false`` on the ``AuthHandler``:
