.. _man-forms:

################
Dropwizard Forms
################

.. highlight:: text

.. rubric:: The ``dropwizard-forms`` module provides you with a support for multi-part forms
            via Jersey_.

.. _Jersey: https://jersey.github.io/ 

Adding The Bundle
=================

Then, in your application's ``initialize`` method, add a new ``MultiPartBundle`` subclass:

.. code-block:: java

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
    }

.. _man-forms-testing:

Testing
=======

To test resources that utilize multi-part form features, one must add ``MultiPartFeature.class`` to
the ``ResourceTestRule`` as a provider, and register it on the client like the following:

.. code-block:: java

    public class MultiPartTest {
        @ClassRule
        public static final ResourceTestRule resource = ResourceTestRule.builder()
                .addProvider(MultiPartFeature.class)
                .addResource(new TestResource())
                .build();

        @Test
        public void testClientMultipart() {
            final FormDataMultiPart multiPart = new FormDataMultiPart()
                    .field("test-data", "Hello Multipart");
            final String response = resource.target("/test")
                    .register(MultiPartFeature.class)
                    .request()
                    .post(Entity.entity(multiPart, multiPart.getMediaType()), String.class);
            assertThat(response).isEqualTo("Hello Multipart");
        }

        @Path("test")
        public static class TestResource {
            @POST
            @Consumes(MediaType.MULTIPART_FORM_DATA)
            public String post(@FormDataParam("test-data") String testData) {
                return testData;
            }
        }
    }

More Information
================

For additional and more detailed documentation about the Jersey multi-part support, please refer to the
documentation in the `Jersey User Guide`_ and Javadoc_.

.. _Jersey User Guide: https://jersey.github.io/documentation/latest/media.html#multipart 
.. _Javadoc: https://jersey.github.io/apidocs/latest/jersey/org/glassfish/jersey/media/multipart/package-summary.html
