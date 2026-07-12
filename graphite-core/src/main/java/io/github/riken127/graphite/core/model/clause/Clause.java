package io.github.riken127.graphite.core.model.clause;

/** Marker for an immutable top-level query clause. */
public sealed interface Clause
    permits LimitClause,
        MatchClause,
        OrderByClause,
        ReturnClause,
        SkipClause,
        UnwindClause,
        WhereClause,
        WithClause {}
