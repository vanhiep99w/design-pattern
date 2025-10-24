package com.designpatterns.showcase.builder;

import com.designpatterns.showcase.builder.query.Query;
import com.designpatterns.showcase.builder.query.QueryBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {

    @Test
    void shouldBuildSimpleSelectQuery() {
        Query query = QueryBuilder.select("id", "name", "email")
                .from("users")
                .build();

        assertEquals("SELECT id, name, email FROM users", query.getSql());
        assertTrue(query.getParameters().isEmpty());
    }

    @Test
    void shouldBuildSelectAllQuery() {
        Query query = QueryBuilder.select()
                .from("products")
                .build();

        assertEquals("SELECT * FROM products", query.getSql());
    }

    @Test
    void shouldBuildQueryWithWhereClause() {
        Query query = QueryBuilder.select("*")
                .from("orders")
                .where("status", "=", "PENDING")
                .build();

        assertTrue(query.getSql().contains("WHERE status = :param0"));
        assertEquals("PENDING", query.getParameters().get("param0"));
    }

    @Test
    void shouldBuildQueryWithMultipleWhereConditions() {
        Query query = QueryBuilder.select("*")
                .from("users")
                .where("active", "=", true)
                .andWhere("role", "=", "ADMIN")
                .build();

        assertTrue(query.getSql().contains("WHERE active = :param0 AND role = :param1"));
        assertEquals(true, query.getParameters().get("param0"));
        assertEquals("ADMIN", query.getParameters().get("param1"));
    }

    @Test
    void shouldBuildQueryWithOrCondition() {
        Query query = QueryBuilder.select("*")
                .from("products")
                .where("category", "=", "Electronics")
                .orWhere("category", "=", "Computers")
                .build();

        assertTrue(query.getSql().contains("WHERE category = :param0 OR category = :param1"));
    }

    @Test
    void shouldBuildQueryWithInClause() {
        List<String> statuses = Arrays.asList("PENDING", "PROCESSING", "SHIPPED");
        Query query = QueryBuilder.select("*")
                .from("orders")
                .whereIn("status", statuses)
                .build();

        assertTrue(query.getSql().contains("WHERE status IN (:param0)"));
        assertEquals(statuses, query.getParameters().get("param0"));
    }

    @Test
    void shouldBuildQueryWithBetweenClause() {
        Query query = QueryBuilder.select("*")
                .from("orders")
                .whereBetween("total_amount", 100, 500)
                .build();

        assertTrue(query.getSql().contains("WHERE total_amount BETWEEN :param0 AND :param1"));
        assertEquals(100, query.getParameters().get("param0"));
        assertEquals(500, query.getParameters().get("param1"));
    }

    @Test
    void shouldBuildQueryWithNullCheck() {
        Query query = QueryBuilder.select("*")
                .from("users")
                .whereNull("deleted_at")
                .build();

        assertTrue(query.getSql().contains("WHERE deleted_at IS NULL"));
    }

    @Test
    void shouldBuildQueryWithNotNullCheck() {
        Query query = QueryBuilder.select("*")
                .from("users")
                .whereNotNull("email_verified_at")
                .build();

        assertTrue(query.getSql().contains("WHERE email_verified_at IS NOT NULL"));
    }

    @Test
    void shouldBuildQueryWithJoin() {
        Query query = QueryBuilder.select("orders.id", "users.name")
                .from("orders")
                .innerJoin("users", "orders.user_id = users.id")
                .build();

        assertTrue(query.getSql().contains("INNER JOIN users ON orders.user_id = users.id"));
    }

    @Test
    void shouldBuildQueryWithLeftJoin() {
        Query query = QueryBuilder.select("*")
                .from("users")
                .leftJoin("orders", "users.id = orders.user_id")
                .build();

        assertTrue(query.getSql().contains("LEFT JOIN orders ON users.id = orders.user_id"));
    }

    @Test
    void shouldBuildQueryWithRightJoin() {
        Query query = QueryBuilder.select("*")
                .from("orders")
                .rightJoin("users", "orders.user_id = users.id")
                .build();

        assertTrue(query.getSql().contains("RIGHT JOIN users ON orders.user_id = users.id"));
    }

    @Test
    void shouldBuildQueryWithMultipleJoins() {
        Query query = QueryBuilder.select("*")
                .from("orders")
                .innerJoin("users", "orders.user_id = users.id")
                .leftJoin("order_items", "orders.id = order_items.order_id")
                .build();

        assertTrue(query.getSql().contains("INNER JOIN users"));
        assertTrue(query.getSql().contains("LEFT JOIN order_items"));
    }

    @Test
    void shouldBuildQueryWithGroupBy() {
        Query query = QueryBuilder.select("category", "COUNT(*)")
                .from("products")
                .groupBy("category")
                .build();

        assertTrue(query.getSql().contains("GROUP BY category"));
    }

    @Test
    void shouldBuildQueryWithMultipleGroupBy() {
        Query query = QueryBuilder.select("category", "brand", "COUNT(*)")
                .from("products")
                .groupBy("category", "brand")
                .build();

        assertTrue(query.getSql().contains("GROUP BY category, brand"));
    }

    @Test
    void shouldBuildQueryWithOrderBy() {
        Query query = QueryBuilder.select("*")
                .from("products")
                .orderBy("price")
                .build();

        assertTrue(query.getSql().contains("ORDER BY price ASC"));
    }

    @Test
    void shouldBuildQueryWithOrderByDesc() {
        Query query = QueryBuilder.select("*")
                .from("products")
                .orderBy("created_at", "DESC")
                .build();

        assertTrue(query.getSql().contains("ORDER BY created_at DESC"));
    }

    @Test
    void shouldBuildQueryWithMultipleOrderBy() {
        Query query = QueryBuilder.select("*")
                .from("products")
                .orderBy("category", "ASC")
                .orderBy("price", "DESC")
                .build();

        assertTrue(query.getSql().contains("ORDER BY category ASC, price DESC"));
    }

    @Test
    void shouldBuildQueryWithLimit() {
        Query query = QueryBuilder.select("*")
                .from("products")
                .limit(10)
                .build();

        assertTrue(query.getSql().contains("LIMIT 10"));
    }

    @Test
    void shouldBuildQueryWithOffset() {
        Query query = QueryBuilder.select("*")
                .from("products")
                .limit(10)
                .offset(20)
                .build();

        assertTrue(query.getSql().contains("LIMIT 10"));
        assertTrue(query.getSql().contains("OFFSET 20"));
    }

    @Test
    void shouldBuildComplexQuery() {
        Query query = QueryBuilder.select("orders.id", "users.name", "SUM(order_items.subtotal)")
                .from("orders")
                .innerJoin("users", "orders.user_id = users.id")
                .leftJoin("order_items", "orders.id = order_items.order_id")
                .where("orders.status", "=", "COMPLETED")
                .whereNotNull("orders.shipped_at")
                .groupBy("orders.id", "users.name")
                .orderBy("SUM(order_items.subtotal)", "DESC")
                .limit(10)
                .build();

        String sql = query.getSql();
        assertTrue(sql.contains("SELECT orders.id, users.name, SUM(order_items.subtotal)"));
        assertTrue(sql.contains("FROM orders"));
        assertTrue(sql.contains("INNER JOIN users"));
        assertTrue(sql.contains("LEFT JOIN order_items"));
        assertTrue(sql.contains("WHERE orders.status = :param0"));
        assertTrue(sql.contains("GROUP BY orders.id, users.name"));
        assertTrue(sql.contains("ORDER BY SUM(order_items.subtotal) DESC"));
        assertTrue(sql.contains("LIMIT 10"));
    }

    @Test
    void shouldThrowExceptionWhenTableNameMissing() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            QueryBuilder.select("*")
                    .build();
        });

        assertEquals("Table name is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInClauseEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            QueryBuilder.select("*")
                    .from("orders")
                    .whereIn("status", Arrays.asList())
                    .build();
        });

        assertEquals("Values list cannot be empty for IN clause", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLimitNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            QueryBuilder.select("*")
                    .from("products")
                    .limit(0)
                    .build();
        });

        assertEquals("Limit must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOffsetNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            QueryBuilder.select("*")
                    .from("products")
                    .offset(-1)
                    .build();
        });

        assertEquals("Offset cannot be negative", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOrderDirectionInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            QueryBuilder.select("*")
                    .from("products")
                    .orderBy("price", "INVALID")
                    .build();
        });

        assertEquals("Order direction must be ASC or DESC", exception.getMessage());
    }

    @Test
    void shouldSupportMethodChaining() {
        QueryBuilder builder = QueryBuilder.select("*");

        QueryBuilder result1 = builder.from("users");
        QueryBuilder result2 = result1.where("active", "=", true);
        QueryBuilder result3 = result2.orderBy("name");

        assertSame(builder, result1);
        assertSame(result1, result2);
        assertSame(result2, result3);
    }
}
