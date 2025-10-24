package com.designpatterns.showcase.builder.query;

import java.util.Map;
import java.util.Objects;

public class Query {

    private final String sql;
    private final Map<String, Object> parameters;

    public Query(String sql, Map<String, Object> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return Objects.equals(sql, query.sql) &&
                Objects.equals(parameters, query.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, parameters);
    }

    @Override
    public String toString() {
        return "Query{" +
                "sql='" + sql + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
