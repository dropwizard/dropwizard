package com.yammer.dropwizard.db;

import org.skife.jdbi.v2.Binding;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.RewrittenStatement;

class NamePrependingStatementRewriter extends ColonPrefixNamedParamStatementRewriter {
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
            return super.rewrite(query.toString(), params, ctx);
        }
        return super.rewrite(sql, params, ctx);
    }
}
