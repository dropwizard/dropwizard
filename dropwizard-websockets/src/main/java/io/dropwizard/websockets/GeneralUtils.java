package io.dropwizard.websockets;

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

    @FunctionalInterface
    public interface RunnableCheckException {
        void run() throws Exception;
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
    public static Runnable rethrow(RunnableCheckException r) {
        return () -> {
            try {
                r.run();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
