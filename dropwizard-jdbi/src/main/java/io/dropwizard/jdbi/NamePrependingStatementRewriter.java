package io.dropwizard.jdbi;

import org.skife.jdbi.v2.Binding;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.RewrittenStatement;
import org.skife.jdbi.v2.tweak.StatementRewriter;

public class NamePrependingStatementRewriter implements StatementRewriter {
    private final StatementRewriter rewriter;

    public NamePrependingStatementRewriter(StatementRewriter rewriter) {
        this.rewriter = rewriter;
    }

    @Override
    public RewrittenStatement rewrite(String sql, Binding params, StatementContext ctx) {
        if ((ctx.getSqlObjectType() != null) && (ctx.getSqlObjectMethod() != null)) {
            final StringBuilder query = new StringBuilder(sql.length() + 100);
            query.append("/* ");
            final String className = ctx.getSqlObjectType().getSimpleName();
            if (!className.isEmpty()) {
                query.append(className).append('.');
            }
            query.append(ctx.getSqlObjectMethod().getName());
            query.append(" */ ");
            query.append(sql);
            return rewriter.rewrite(query.toString(), params, ctx);
        }
        return rewriter.rewrite(sql, params, ctx);
    }
}
