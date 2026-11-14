package com.example.learningplatform.config;

import com.example.learningplatform.dto.request.CreateAnswerOptionRequest;
import com.example.learningplatform.dto.request.CreateAssignmentRequest;
import com.example.learningplatform.dto.request.CreateCategoryRequest;
import com.example.learningplatform.dto.request.CreateCourseRequest;
import com.example.learningplatform.dto.request.CreateLessonRequest;
import com.example.learningplatform.dto.request.CreateModuleRequest;
import com.example.learningplatform.dto.request.CreateQuestionRequest;
import com.example.learningplatform.dto.request.CreateQuizRequest;
import com.example.learningplatform.dto.request.CreateReviewRequest;
import com.example.learningplatform.dto.request.CreateSubmissionRequest;
import com.example.learningplatform.dto.request.CreateTagRequest;
import com.example.learningplatform.dto.request.CreateUserRequest;
import com.example.learningplatform.dto.request.GradeSubmissionRequest;
import com.example.learningplatform.dto.request.QuizAnswerRequest;
import com.example.learningplatform.dto.request.TakeQuizRequest;
import com.example.learningplatform.dto.request.UpsertProfileRequest;
import com.example.learningplatform.dto.response.AnswerOptionResponse;
import com.example.learningplatform.dto.response.CourseDetailsResponse;
import com.example.learningplatform.dto.response.QuestionResponse;
import com.example.learningplatform.dto.response.QuizResponse;
import com.example.learningplatform.dto.response.UserResponse;
import com.example.learningplatform.enums.QuestionType;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.repository.UserRepository;
import com.example.learningplatform.service.AssignmentService;
import com.example.learningplatform.service.CategoryService;
import com.example.learningplatform.service.CourseService;
import com.example.learningplatform.service.EnrollmentService;
import com.example.learningplatform.service.LessonService;
import com.example.learningplatform.service.ModuleService;
import com.example.learningplatform.service.QuizService;
import com.example.learningplatform.service.ReviewService;
import com.example.learningplatform.service.SubmissionService;
import com.example.learningplatform.service.TagService;
import com.example.learningplatform.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
public class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final CourseService courseService;
    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final AssignmentService assignmentService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;
    private final ReviewService reviewService;

    public DemoDataLoader(
            UserRepository userRepository,
            UserService userService,
            CategoryService categoryService,
            TagService tagService,
            CourseService courseService,
            ModuleService moduleService,
            LessonService lessonService,
            AssignmentService assignmentService,
            EnrollmentService enrollmentService,
            SubmissionService submissionService,
            QuizService quizService,
            ReviewService reviewService
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.courseService = courseService;
        this.moduleService = moduleService;
        this.lessonService = lessonService;
        this.assignmentService = assignmentService;
        this.enrollmentService = enrollmentService;
        this.submissionService = submissionService;
        this.quizService = quizService;
        this.reviewService = reviewService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase("teacher@example.com")) {
            log.info("Демо-данные уже загружены");
            return;
        }
        UserResponse teacher = userService.create(new CreateUserRequest(
                "Анна Преподаватель",
                "teacher@example.com",
                Role.TEACHER,
                new UpsertProfileRequest("Преподаватель ORM и Hibernate", null)));
        UserResponse student = userService.create(new CreateUserRequest(
                "Иван Студент",
                "student1@example.com",
                Role.STUDENT,
                new UpsertProfileRequest("Студент первого потока", null)));
        UserResponse secondStudent = userService.create(new CreateUserRequest(
                "Мария Студент",
                "student2@example.com",
                Role.STUDENT,
                null));
        userService.create(new CreateUserRequest(
                "Администратор",
                "admin@example.com",
                Role.ADMIN,
                null));

        var programming = categoryService.create(new CreateCategoryRequest("Программирование"));
        var databases = categoryService.create(new CreateCategoryRequest("Базы данных"));
        var java = tagService.create(new CreateTagRequest("Java"));
        var hibernate = tagService.create(new CreateTagRequest("Hibernate"));
        var orm = tagService.create(new CreateTagRequest("ORM"));
        var sql = tagService.create(new CreateTagRequest("SQL"));

        CourseDetailsResponse course = courseService.create(new CreateCourseRequest(
                "Основы Hibernate",
                "Курс про отображение объектов, сессии и ленивую загрузку.",
                programming.id(),
                teacher.id(),
                "6 недель",
                LocalDate.of(2026, 11, 1),
                List.of(java.id(), hibernate.id(), orm.id())));

        var introduction = moduleService.create(course.id(), new CreateModuleRequest(
                "Введение в ORM",
                "Зачем нужен ORM и как устроена сессия.",
                0));
        lessonService.create(introduction.id(), new CreateLessonRequest(
                "Что такое ORM",
                "ORM связывает объекты приложения со строками таблиц.",
                "https://example.com/orm"));
        var sessionLesson = lessonService.create(introduction.id(), new CreateLessonRequest(
                "Session и транзакции",
                "Изменения попадают в базу в конце транзакции.",
                null));
        var assignment = assignmentService.create(sessionLesson.id(), new CreateAssignmentRequest(
                "Описать жизненный цикл Session",
                "Кратко опишите состояния transient, persistent и detached.",
                LocalDate.now().plusDays(14),
                100));

        var relations = moduleService.create(course.id(), new CreateModuleRequest(
                "Связи и ленивая загрузка",
                "OneToMany, ManyToMany и FetchType.LAZY.",
                1));
        var lazyLesson = lessonService.create(relations.id(), new CreateLessonRequest(
                "OneToMany и FetchType.LAZY",
                "Коллекция модулей курса не загружается, пока к ней не обратились внутри транзакции.",
                null));
        assignmentService.create(lazyLesson.id(), new CreateAssignmentRequest(
                "Разобрать связи курса",
                "Опишите, какие связи на схеме курса ленивые.",
                LocalDate.now().plusDays(21),
                100));

        QuizResponse quiz = quizService.create(introduction.id(), new CreateQuizRequest("Тест по модулю 1", 15));
        quiz = quizService.addQuestion(quiz.id(), new CreateQuestionRequest(
                "Какая загрузка используется у @OneToMany по умолчанию?",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new CreateAnswerOptionRequest("EAGER", false),
                        new CreateAnswerOptionRequest("LAZY", true),
                        new CreateAnswerOptionRequest("SUBSELECT", false))));
        quiz = quizService.addQuestion(quiz.id(), new CreateQuestionRequest(
                "Что связывает сущность Enrollment?",
                QuestionType.MULTIPLE_CHOICE,
                List.of(
                        new CreateAnswerOptionRequest("Студента", true),
                        new CreateAnswerOptionRequest("Курс", true),
                        new CreateAnswerOptionRequest("Пароль пользователя", false))));

        courseService.create(new CreateCourseRequest(
                "SQL для разработчиков",
                "Практический курс по запросам и связям.",
                databases.id(),
                teacher.id(),
                "4 недели",
                LocalDate.of(2026, 12, 1),
                List.of(sql.id())));

        enrollmentService.enroll(course.id(), student.id());
        enrollmentService.enroll(course.id(), secondStudent.id());
        var submission = submissionService.submit(assignment.id(), new CreateSubmissionRequest(
                student.id(),
                "Transient — объект ещё не связан с сессией. Persistent — находится в persistence context. Detached — сессия уже закрыта."));
        submissionService.grade(submission.id(), new GradeSubmissionRequest(
                teacher.id(),
                90,
                "Хорошее объяснение, не хватает примера merge."));

        quizService.take(quiz.id(), new TakeQuizRequest(student.id(), answersFor(quiz)));
        reviewService.create(course.id(), new CreateReviewRequest(
                student.id(),
                5,
                "Понятно объяснили ленивую загрузку."));
        log.info("Демо-данные загружены: курс «{}», преподаватель {}, студенты {} и {}",
                course.title(), teacher.email(), student.email(), secondStudent.email());
    }

    private List<QuizAnswerRequest> answersFor(QuizResponse quiz) {
        return quiz.questions().stream()
                .map(question -> new QuizAnswerRequest(question.id(), correctOptionIds(question)))
                .toList();
    }

    private List<Long> correctOptionIds(QuestionResponse question) {
        return question.options().stream()
                .filter(AnswerOptionResponse::correct)
                .map(AnswerOptionResponse::id)
                .toList();
    }
}
