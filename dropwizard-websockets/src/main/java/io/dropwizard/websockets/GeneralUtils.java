package io.dropwizard.websockets;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneralUtils {
    
    @FunctionalInterface
    public interface ConsumerCheckException<T> {
        void accept(T elem) throws Exception;
    }

    @FunctionalInterface
    public interface RunnableCheckException {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface SupplierCheckException<T> {
        T get() throws Exception;
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
    public static <T> Supplier<T> rethrow(SupplierCheckException<T> c) {
        return () -> {
            try {
                return c.get();
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
