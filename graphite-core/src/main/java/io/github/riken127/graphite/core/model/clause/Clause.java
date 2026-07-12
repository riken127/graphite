package io.github.riken127.graphite.core.model.clause;

/** Marker for an immutable top-level query clause. */
public sealed interface Clause
    permits CallClause,
        CreateClause,
        DeleteClause,
        LimitClause,
        MatchClause,
        MergeClause,
        OrderByClause,
        RemoveClause,
        ReturnClause,
        SetClause,
        SkipClause,
        SubqueryClause,
        UnwindClause,
        WhereClause,
        WithClause {}
