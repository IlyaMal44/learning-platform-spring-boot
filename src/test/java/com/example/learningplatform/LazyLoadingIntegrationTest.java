package com.example.learningplatform;

import com.example.learningplatform.domain.Course;
import com.example.learningplatform.support.ServiceTestSupport;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LazyLoadingIntegrationTest extends ServiceTestSupport {

    @Test
    @DisplayName("Коллекция модулей ленивая и недоступна после закрытия сессии")
    void modulesStayLazyOutsideSession() {
        var course = courseWithAssignment(teacher(), category());
        Course loaded = courseRepository.findById(course.id()).orElseThrow();

        assertThatThrownBy(() -> loaded.getModules().size())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    @DisplayName("Сервис читает структуру курса внутри транзакции")
    void serviceReadsStructureInsideTransaction() {
        var course = courseWithAssignment(teacher(), category());
        var details = courseService.get(course.id());
        assertThat(details.modules()).hasSize(1);
        assertThat(details.modules().getFirst().lessons()).hasSize(1);
    }
}
