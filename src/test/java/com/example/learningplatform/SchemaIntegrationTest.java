package com.example.learningplatform;

import com.example.learningplatform.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Hibernate создаёт таблицы всех сущностей и связующую таблицу тегов")
    void createsExpectedTables() throws Exception {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (resultSet.next()) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }
        }
        assertThat(tables).contains(
                "users",
                "profiles",
                "categories",
                "courses",
                "tags",
                "course_tags",
                "enrollments",
                "course_reviews",
                "modules",
                "lessons",
                "assignments",
                "submissions",
                "quizzes",
                "questions",
                "answer_options",
                "quiz_submissions");
    }
}
