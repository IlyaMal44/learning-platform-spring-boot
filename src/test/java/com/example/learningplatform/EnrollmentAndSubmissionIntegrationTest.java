package com.example.learningplatform;

import com.example.learningplatform.dto.request.CreateAssignmentRequest;
import com.example.learningplatform.dto.request.CreateReviewRequest;
import com.example.learningplatform.dto.request.CreateSubmissionRequest;
import com.example.learningplatform.dto.request.GradeSubmissionRequest;
import com.example.learningplatform.dto.request.UpdateEnrollmentRequest;
import com.example.learningplatform.enums.EnrollmentStatus;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.support.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentAndSubmissionIntegrationTest extends ServiceTestSupport {

    @Test
    @DisplayName("Студент записывается, видит курс, курс видит студента, затем отписывается")
    void enrollListsAndUnenrolls() {
        var teacher = teacher();
        var student = student();
        var course = course(teacher, category());

        var enrollment = enrollmentService.enroll(course.id(), student.id());
        assertThat(enrollment.status()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollmentService.listByCourse(course.id())).extracting(item -> item.studentId()).containsExactly(student.id());
        assertThat(enrollmentService.listByStudent(student.id())).extracting(item -> item.courseId()).containsExactly(course.id());
        assertThat(enrollmentService.listStudents(course.id())).extracting(item -> item.id()).containsExactly(student.id());

        var completed = enrollmentService.updateStatus(enrollment.id(), new UpdateEnrollmentRequest(EnrollmentStatus.COMPLETED));
        assertThat(completed.status()).isEqualTo(EnrollmentStatus.COMPLETED);

        enrollmentService.unenroll(course.id(), student.id());
        assertThat(enrollmentService.listByCourse(course.id())).isEmpty();
        assertThat(enrollmentRepository.findById(enrollment.id())).isEmpty();
    }

    @Test
    @DisplayName("Повторная запись и запись преподавателя отклоняются")
    void rejectsDuplicateAndTeacherEnrollment() {
        var teacher = teacher();
        var student = student();
        var course = course(teacher, category());
        enrollmentService.enroll(course.id(), student.id());

        assertThatThrownBy(() -> enrollmentService.enroll(course.id(), student.id())).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> enrollmentService.enroll(course.id(), teacher.id())).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Студент сдаёт решение один раз, преподаватель ставит оценку")
    void submitsOnceAndGrades() {
        var teacher = teacher();
        var student = student();
        var course = courseWithAssignment(teacher, category());
        enrollmentService.enroll(course.id(), student.id());
        var assignment = course.modules().getFirst().lessons().getFirst().assignments().getFirst();

        var submission = submissionService.submit(assignment.id(), new CreateSubmissionRequest(student.id(), "Моё решение"));
        assertThat(submission.score()).isNull();
        assertThatThrownBy(() -> submissionService.submit(assignment.id(), new CreateSubmissionRequest(student.id(), "Ещё раз")))
                .isInstanceOf(ConflictException.class);

        var graded = submissionService.grade(submission.id(), new GradeSubmissionRequest(teacher.id(), 80, "Хорошо"));
        assertThat(graded.score()).isEqualTo(80);
        assertThat(graded.feedback()).isEqualTo("Хорошо");
        assertThat(submissionService.listByAssignment(assignment.id())).hasSize(1);
        assertThat(submissionService.listByStudent(student.id())).extracting(item -> item.id()).contains(submission.id());
        assertThat(submissionRepository.findById(submission.id()).orElseThrow().getScore()).isEqualTo(80);
    }

    @Test
    @DisplayName("Нельзя сдать работу без записи, после дедлайна и с оценкой выше максимума")
    void submissionRules() {
        var teacher = teacher();
        var student = student();
        var outsider = student();
        var course = courseWithAssignment(teacher, category());
        var lessonId = course.modules().getFirst().lessons().getFirst().id();
        var openAssignment = course.modules().getFirst().lessons().getFirst().assignments().getFirst();
        var expired = assignmentService.create(lessonId, new CreateAssignmentRequest(
                "Просроченное",
                "Уже поздно",
                LocalDate.now().minusDays(2),
                10));
        enrollmentService.enroll(course.id(), student.id());

        assertThatThrownBy(() -> submissionService.submit(openAssignment.id(), new CreateSubmissionRequest(outsider.id(), "Нет записи")))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> submissionService.submit(expired.id(), new CreateSubmissionRequest(student.id(), "Поздно")))
                .isInstanceOf(BusinessRuleException.class);

        var submission = submissionService.submit(openAssignment.id(), new CreateSubmissionRequest(student.id(), "Вовремя"));
        assertThatThrownBy(() -> submissionService.grade(submission.id(), new GradeSubmissionRequest(teacher.id(), 101, null)))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> submissionService.grade(submission.id(), new GradeSubmissionRequest(outsider.id(), 10, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Отзыв доступен записанному студенту и только один раз")
    void reviewsRequireEnrollmentAndAreUnique() {
        var teacher = teacher();
        var student = student();
        var outsider = student();
        var course = course(teacher, category());
        enrollmentService.enroll(course.id(), student.id());

        assertThatThrownBy(() -> reviewService.create(course.id(), new CreateReviewRequest(outsider.id(), 4, "Нет")))
                .isInstanceOf(BusinessRuleException.class);

        var review = reviewService.create(course.id(), new CreateReviewRequest(student.id(), 5, "Отличный курс"));
        assertThat(review.rating()).isEqualTo(5);
        assertThat(reviewService.listByCourse(course.id())).hasSize(1);
        assertThatThrownBy(() -> reviewService.create(course.id(), new CreateReviewRequest(student.id(), 3, "Ещё один")))
                .isInstanceOf(ConflictException.class);
    }
}
