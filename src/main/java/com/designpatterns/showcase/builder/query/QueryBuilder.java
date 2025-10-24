package com.designpatterns.showcase.builder.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {

    private String table;
    private final List<String> selectColumns;
    private final List<String> joins;
    private final List<WhereCondition> whereConditions;
    private final List<String> groupByColumns;
    private final List<String> orderByColumns;
    private Integer limit;
    private Integer offset;
    private final Map<String, Object> parameters;

    private QueryBuilder() {
        this.selectColumns = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.whereConditions = new ArrayList<>();
        this.groupByColumns = new ArrayList<>();
        this.orderByColumns = new ArrayList<>();
        this.parameters = new HashMap<>();
    }

    public static QueryBuilder select(String... columns) {
        QueryBuilder builder = new QueryBuilder();
        if (columns.length == 0) {
            builder.selectColumns.add("*");
        } else {
            for (String column : columns) {
                builder.selectColumns.add(column);
            }
        }
        return builder;
    }

    public QueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    public QueryBuilder innerJoin(String table, String condition) {
        this.joins.add("INNER JOIN " + table + " ON " + condition);
        return this;
    }

    public QueryBuilder leftJoin(String table, String condition) {
        this.joins.add("LEFT JOIN " + table + " ON " + condition);
        return this;
    }

    public QueryBuilder rightJoin(String table, String condition) {
        this.joins.add("RIGHT JOIN " + table + " ON " + condition);
        return this;
    }

    public QueryBuilder where(String column, String operator, Object value) {
        String paramName = "param" + whereConditions.size();
        whereConditions.add(new WhereCondition(column, operator, paramName, "AND"));
        parameters.put(paramName, value);
        return this;
    }

    public QueryBuilder andWhere(String column, String operator, Object value) {
        return where(column, operator, value);
    }

    public QueryBuilder orWhere(String column, String operator, Object value) {
        String paramName = "param" + whereConditions.size();
        whereConditions.add(new WhereCondition(column, operator, paramName, "OR"));
        parameters.put(paramName, value);
        return this;
    }

    public QueryBuilder whereIn(String column, List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values list cannot be empty for IN clause");
        }
        String paramName = "param" + whereConditions.size();
        whereConditions.add(new WhereCondition(column, "IN", paramName, "AND"));
        parameters.put(paramName, values);
        return this;
    }

    public QueryBuilder whereBetween(String column, Object start, Object end) {
        String startParam = "param" + whereConditions.size();
        String endParam = "param" + (whereConditions.size() + 1);
        whereConditions.add(new WhereCondition(column, "BETWEEN", startParam + "," + endParam, "AND"));
        parameters.put(startParam, start);
        parameters.put(endParam, end);
        return this;
    }

    public QueryBuilder whereNull(String column) {
        whereConditions.add(new WhereCondition(column, "IS NULL", null, "AND"));
        return this;
    }

    public QueryBuilder whereNotNull(String column) {
        whereConditions.add(new WhereCondition(column, "IS NOT NULL", null, "AND"));
        return this;
    }

    public QueryBuilder groupBy(String... columns) {
        for (String column : columns) {
            groupByColumns.add(column);
        }
        return this;
    }

    public QueryBuilder orderBy(String column) {
        orderByColumns.add(column + " ASC");
        return this;
    }

    public QueryBuilder orderBy(String column, String direction) {
        if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("Order direction must be ASC or DESC");
        }
        orderByColumns.add(column + " " + direction.toUpperCase());
        return this;
    }

    public QueryBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limit = limit;
        return this;
    }

    public QueryBuilder offset(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative");
        }
        this.offset = offset;
        return this;
    }

    public Query build() {
        if (table == null || table.trim().isEmpty()) {
            throw new IllegalStateException("Table name is required");
        }

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append(String.join(", ", selectColumns));

        sql.append(" FROM ").append(table);

        if (!joins.isEmpty()) {
            sql.append(" ");
            sql.append(String.join(" ", joins));
        }

        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < whereConditions.size(); i++) {
                if (i > 0) {
                    sql.append(" ").append(whereConditions.get(i).getLogicalOperator()).append(" ");
                }
                sql.append(whereConditions.get(i).toSql());
            }
        }

        if (!groupByColumns.isEmpty()) {
            sql.append(" GROUP BY ");
            sql.append(String.join(", ", groupByColumns));
        }

        if (!orderByColumns.isEmpty()) {
            sql.append(" ORDER BY ");
            sql.append(String.join(", ", orderByColumns));
        }

        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }

        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
        }

        return new Query(sql.toString(), new HashMap<>(parameters));
    }

    private static class WhereCondition {
        private final String column;
        private final String operator;
        private final String paramName;
        private final String logicalOperator;

        public WhereCondition(String column, String operator, String paramName, String logicalOperator) {
            this.column = column;
            this.operator = operator;
            this.paramName = paramName;
            this.logicalOperator = logicalOperator;
        }

        public String toSql() {
            if (operator.equals("IS NULL") || operator.equals("IS NOT NULL")) {
                return column + " " + operator;
            } else if (operator.equals("IN")) {
                return column + " IN (:" + paramName + ")";
            } else if (operator.equals("BETWEEN")) {
                String[] params = paramName.split(",");
                return column + " BETWEEN :" + params[0] + " AND :" + params[1];
            } else {
                return column + " " + operator + " :" + paramName;
            }
        }

        public String getLogicalOperator() {
            return logicalOperator;
        }
    }
}
