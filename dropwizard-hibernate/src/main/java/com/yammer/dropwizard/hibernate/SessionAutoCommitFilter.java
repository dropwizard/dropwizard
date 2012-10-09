package com.yammer.dropwizard.hibernate;

import com.yammer.dropwizard.logging.Log;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import sun.misc.Unsafe;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.hibernate.context.internal.ManagedSessionContext.hasBind;
import static org.hibernate.context.internal.ManagedSessionContext.unbind;

public class SessionAutoCommitFilter implements Filter {
    private static final Log LOG = Log.forClass(SessionAutoCommitFilter.class);

    private static Unsafe getUnsafe() {
        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception ex) {
            throw new RuntimeException("can't get Unsafe instance", ex);
        }
    }

    private final Unsafe unsafe;
    private final SessionFactory factory;

    public SessionAutoCommitFilter(SessionFactory factory) {
        this.factory = factory;
        this.unsafe = getUnsafe();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        Session session = null;
        Transaction txn = null;
        try {
            chain.doFilter(request, response);
            if (hasBind(factory)) {
                session = factory.getCurrentSession();
                txn = session.getTransaction();
                if (txn.isActive()) {
                    txn.commit();
                }
                session.close();
            }
        } catch (Exception e) {
            LOG.warn(e, "Uh oh");
            if ((session != null) && (txn != null) && txn.isActive()) {
                txn.rollback();
            }
            unsafe.throwException(e);
        } finally {
            if ((session != null) && session.isOpen()) {
                session.close();
            }
            unbind(factory);
        }
    }

    @Override
    public void destroy() {
    }
}
