package com.yammer.dropwizard.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToCreateStatementException;
import org.skife.jdbi.v2.tweak.StatementLocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "NestedAssignment"})
class ScopedStatementLocator implements StatementLocator {
    public static boolean isSql(String sql) {
        final String local = sql.substring(0, 7).toLowerCase();
        return local.startsWith("insert ")
                || local.startsWith("update ")
                || local.startsWith("select ")
                || local.startsWith("call ")
                || local.startsWith("delete ")
                || local.startsWith("create ")
                || local.startsWith("alter ")
                || local.startsWith("drop ");
    }

    @Override
    public String locate(String name, StatementContext ctx) {
        if (isSql(name)) {
            return name;
        }
        final ClassLoader loader = selectClassLoader();
        InputStream input = loader.getResourceAsStream(name);
        BufferedReader reader = null;
        try {
            if (input == null) {
                input = loader.getResourceAsStream(name + ".sql");
            }

            if ((input == null) && (ctx.getSqlObjectType() != null)) {
                input = loader.getResourceAsStream(ctx.getSqlObjectType()
                                                      .getCanonicalName() + '.' + name + ".sql");
            }

            if (input == null) {
                return name;
            }

            final StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(input));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!isComment(line)) {
                        buffer.append(line).append(' ');
                    }
                }
            } catch (IOException e) {
                throw new UnableToCreateStatementException(e.getMessage(), e, ctx);
            }

            return buffer.toString();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
                // nothing we can do here :-(
            }
        }
    }

    private static ClassLoader selectClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return ScopedStatementLocator.class.getClassLoader();
    }

    private static boolean isComment(final String line) {
        return line.startsWith("#") || line.startsWith("--") || line.startsWith("//");
    }
}
