package io.dropwizard.dropwizard.websockets;

import java.util.function.Consumer;

/**
 *
 * @author eitanya
 */
public class GeneralUtils {
    
    @FunctionalInterface
    public interface ConsumerCheckException<T> {
        void accept(T elem) throws Exception;
    }

    public static <T> Consumer<T> rethrow(ConsumerCheckException<T> c) {
        return t -> {
            try {
                c.accept(t);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
