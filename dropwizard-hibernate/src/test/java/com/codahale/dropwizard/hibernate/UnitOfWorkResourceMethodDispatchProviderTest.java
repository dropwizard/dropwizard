package com.codahale.dropwizard.hibernate;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.SessionFactory;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnitOfWorkResourceMethodDispatchProviderTest {
    @SuppressWarnings("UnusedDeclaration")
    private static class Example {
        @UnitOfWork
        public void annotated() {

        }

        public void nonAnnotated() {}

    }

    private final ResourceMethodDispatchProvider underlying =
            mock(ResourceMethodDispatchProvider.class);
    private final SessionFactory sessionFactory = mock(SessionFactory.class);
    private final UnitOfWorkResourceMethodDispatchProvider provider =
            new UnitOfWorkResourceMethodDispatchProvider(underlying, sessionFactory);

    @Test
    public void ignoresNonAnnotatedMethods() throws Exception {
        final AbstractResourceMethod resourceMethod = mock(AbstractResourceMethod.class);
        when(resourceMethod.getMethod()).thenReturn(Example.class.getDeclaredMethod("nonAnnotated"));

        final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(underlying.create(resourceMethod)).thenReturn(dispatcher);

        assertThat(provider.create(resourceMethod))
                .isEqualTo(dispatcher);
    }

    @Test
    public void decoratesAnnotatedMethods() throws Exception {
        final AbstractResourceMethod resourceMethod = mock(AbstractResourceMethod.class);
        final Method method = Example.class.getDeclaredMethod("annotated");
        when(resourceMethod.getMethod()).thenReturn(method);

        final RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(underlying.create(resourceMethod)).thenReturn(dispatcher);

        final UnitOfWorkRequestDispatcher decorator = (UnitOfWorkRequestDispatcher) provider.create(resourceMethod);

        assertThat(decorator.getSessionFactory())
                .isEqualTo(sessionFactory);

        assertThat(decorator.getDispatcher())
                .isEqualTo(dispatcher);

        assertThat(decorator.getUnitOfWork())
                .isEqualTo(method.getAnnotation(UnitOfWork.class));
    }
}
