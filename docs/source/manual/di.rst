.. _man-di:

################################
Dropwizard Dependency Injection
################################

.. highlight:: text

.. rubric:: Dropwizard provides you with simple dependency injection mechanism, using HK2,
            out-of-the-box, and you can add support for more advanced DI by using `Guice bundle <https://github.com/xvik/dropwizard-guicey>`_.

.. _man-di-hk2:

Dependency Injection Using HK2
==============================

The underlying library for out-of-the-box dependency injection mechanism in Dropwizard is Eclipse's HK2_, a CDI-compliant dependency injection framework.

.. _HK2: https://github.com/eclipse-ee4j/glassfish-hk2

To create a dependency injection configuration that can be overriden during test execution for mocking purposes,
put it into your app Configuration for bundle to consume:

.. code-block:: java

    public interface DependencyInjectionConfiguration {
        List<Class<?>> getSingletons();
        List<NamedProperty<? extends Object>> getNamedProperties();
    }

    public class NamedProperty<T> {
        private final String id;
        private final T value;
        private final Class<T> clazz;

        @JsonCreator
        public NamedProperty(@JsonProperty("id") String id, @JsonProperty("value") T value, @JsonProperty("clazz") Class<T> clazz) {
            this.id = id;
            this.value = value;
            this.clazz = clazz;
        }

        public String getId() {
            return id;
        }

        public T getValue() {
            return value;
        }

        public Class<T> getClazz() {
            return clazz;
        }
    }

    public class ExampleConfiguration extends Configuration implements DependencyInjectionConfiguration {

        protected Class<?> getUserRepository() {
            return UserRepository.class;
        }

        @Override
        public List<Class<?>> getSingletons() {
            final List<Class<?>> result = new ArrayList();
            result.add(getUserRepository());
            result.add(UserResource.class);

            return result;
        }

        @Override
        public List<NamedProperty<? extends Object>> getNamedProperties() {
            final List<NamedProperty<? extends Object>> result = new ArrayList<>();
            result.add(new NamedProperty<>("dbUser", "dummy_db_user", String.class));

            return result;
        }
    }

Then implement a bundle for DI:

.. code-block:: java

    public class DependencyInjectionBundle implements ConfiguredBundle<DependencyInjectionConfiguration> {

        @Override
        public void run(DependencyInjectionConfiguration configuration, Environment environment) throws Exception {
                environment
                    .jersey()
                    .register(
                        new AbstractBinder() {
                            @Override
                            protected void configure() {
                                for (Class<?> singletonClass : configuration.getSingletons()) {
                                    bindAsContract(singletonClass).in(Singleton.class);
                                }

                                for (NamedProperty<? extends Object> namedProperty : configuration.getNamedProperties()) {
                                    bind((Object) namedProperty.getValue()).to((Class<Object>) namedProperty.getClazz()).named(namedProperty.getId());
                                }
                            }
                        }
                    );
        }
    }

Then, in your application's ``run`` method, create a new ``DependencyInjectionBundle``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {
        final DependencyInjectionBundle dependencyInjectionBundle = new DependencyInjectionBundle();
        dependencyInjectionBundle.run(configuration, environment);
    }

This allows you to use CDI annotations to control your dependency injection:

.. code-block:: java

    @Singleton
    public class UserResource {
        private final UserRepository userRepository;

        @Inject
        public UserResource(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
    }

    @Singleton
    public class UserRepository {
        private final String dbUser;

        @Inject
        public UserRepository(@Named("dbUser") String dbUser) {
            this.dbUser = dbUser;
        }
    }

Then you can provide alternate configuration for testing purposes:

.. code-block:: java

    public class TestConfiguration extends ExampleConfiguration {

        @Override
        protected Class<?> getUserRepository() {
            return MockUserRepository.class;
        }
    }

    @DisplayName("User endpoint")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public class UserControllerTests {
        public static final DropwizardAppExtension<TestConfiguration> app = new DropwizardAppExtension<>(ExampleApplication.class, new TestConfiguration());
    }
