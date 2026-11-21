package io.dropwizard.jersey.validation;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.glassfish.jersey.server.model.Invocable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ConstraintMessageCacheTest {
    private static final long EXPECTED_MAXIMUM_SIZE = 10_000L;

    static final class Request {
        final Map<String, @NotNull String> values = new HashMap<>();
    }

    private static Request requestWithNullValues(String prefix, int count) {
        final Request request = new Request();
        for (int i = 0; i < count; i++) {
            request.values.put(prefix + "-" + i, null);
        }
        return request;
    }

    private static Cache<?, ?> prefixCache() throws Exception {
        final Field field = ConstraintMessage.class.getDeclaredField("PREFIX_CACHE");
        field.setAccessible(true);
        return (Cache<?, ?>) field.get(null);
    }

    @Test
    void prefixCacheStaysBoundedWhenPathsContainClientChosenMapKeys() throws Exception {
        final Validator validator = Validators.newValidator();
        final Invocable invocable = mock(Invocable.class);

        for (ConstraintViolation<Request> violation : validator.validate(requestWithNullValues("bounded", 15_000))) {
            ConstraintMessage.getMessage(violation, invocable);
        }

        final Cache<?, ?> cache = prefixCache();
        cache.cleanUp();
        assertThat(cache.estimatedSize()).isLessThanOrEqualTo(EXPECTED_MAXIMUM_SIZE);
    }

    @Test
    void messagesStayCorrectUnderConcurrentLoadAndEviction() throws Exception {
        final Validator validator = Validators.newValidator();
        final Invocable invocable = mock(Invocable.class);
        final ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            final List<Future<Void>> results = new ArrayList<>();
            for (int thread = 0; thread < 8; thread++) {
                final String prefix = "thread-" + thread;
                final Callable<Void> task = () -> {
                    for (ConstraintViolation<Request> violation : validator.validate(requestWithNullValues(prefix, 3_000))) {
                        assertThat(ConstraintMessage.getMessage(violation, invocable))
                            .isEqualTo(violation.getPropertyPath() + " " + violation.getMessage());
                    }
                    return null;
                };
                results.add(executor.submit(task));
            }
            for (Future<Void> result : results) {
                result.get(120, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
