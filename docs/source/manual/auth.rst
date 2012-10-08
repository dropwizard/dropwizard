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

Authenticators implement the ``Authenticator<C, P>`` interface, which has a single method:

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

    CachingAuthenticator.wrap(ldapAuthenticator,
                              config.getAuthenticationCachePolicy());

Dropwizard can parse Guava's ``CacheBuilderSpec`` from the configuration policy, allowing your
configuration file to look like this:

.. code-block:: yaml

    authenticationCachePolicy: maximumSize=10000, expireAfterAccess=10m

This caches up to 10,000 principals with an LRU policy, evicting stale entries after 10 minutes.

.. _man-auth-basic:

Basic Authentication
====================

The ``BasicAuthProvider`` enables HTTP Basic authentication, and requires an authenticator which
takes instances of ``BasicCredentials``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        environment.addProvider(new BasicAuthProvider<User>(new ExampleAuthenticator(),
                                                            "SUPER SECRET STUFF"));
    }

.. _man-auth-oauth2:

OAuth2
======

The ``OAuthProvider`` enables OAuth2 bearer-token authentication, and requires an authenticator
which takes an instance of ``String``.

.. note:: Because OAuth2 is not finalized, this implementation may change in the future. The
          expectation is that tokens are passed in via the ``Authorization`` header using the
          ``Bearer`` scheme.

.. code-block:: java

    @Override
    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        environment.addProvider(new OAuthProvider<User>(new ExampleAuthenticator(),
                                                            "SUPER SECRET STUFF"));
    }

.. _man-auth-resources:

Protecting Resources
====================

To protect a resource, simply include an ``@Auth``-annotated principal as one of your resource
method parameters:

.. code-block:: java

    @GET
    public SecretPlan getSecretPlan(@Auth User user) {
        return dao.findPlanForUser(user);
    }

If there are no provided credentials for the request, or if the credentials are invalid, the
provider will return a scheme-appropriate ``401 Unauthorized`` response without calling your
resource method.

If you have a resource which is optionally protected (e.g., you want to display a logged-in user's
name but not require login), set the ``required`` attribute of the annotation to ``false``:

.. code-block:: java

    @GET
    public HomepageView getHomepage(@Auth(required = false) User user) {
        return new HomepageView(Optional.fromNullable(user));
    }

If there is no authenticated principal, ``null`` is used instead, and your resource method is still
called.
