package com.example.learningplatform.service;

import com.example.learningplatform.domain.AnswerOption;
import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Module;
import com.example.learningplatform.domain.Question;
import com.example.learningplatform.domain.Quiz;
import com.example.learningplatform.domain.QuizSubmission;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.request.CreateAnswerOptionRequest;
import com.example.learningplatform.dto.request.CreateQuestionRequest;
import com.example.learningplatform.dto.request.CreateQuizRequest;
import com.example.learningplatform.dto.request.TakeQuizRequest;
import com.example.learningplatform.dto.request.UpdateQuizRequest;
import com.example.learningplatform.dto.response.QuizAttemptResponse;
import com.example.learningplatform.dto.response.QuizResponse;
import com.example.learningplatform.dto.response.QuizResultResponse;
import com.example.learningplatform.enums.QuestionType;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.QuizMapper;
import com.example.learningplatform.repository.QuestionRepository;
import com.example.learningplatform.repository.QuizRepository;
import com.example.learningplatform.repository.QuizSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ModuleService moduleService;
    private final UserService userService;
    private final EnrollmentPolicy enrollmentPolicy;
    private final Clock clock;

    public QuizService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuizSubmissionRepository quizSubmissionRepository,
            ModuleService moduleService,
            UserService userService,
            EnrollmentPolicy enrollmentPolicy,
            Clock clock
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.moduleService = moduleService;
        this.userService = userService;
        this.enrollmentPolicy = enrollmentPolicy;
        this.clock = clock;
    }

    @Transactional
    public QuizResponse create(Long moduleId, CreateQuizRequest request) {
        Module module = moduleService.requireModule(moduleId);
        if (quizRepository.existsByModuleId(moduleId)) {
            throw new ConflictException("У модуля уже есть тест");
        }
        Quiz quiz = new Quiz();
        quiz.setModule(module);
        quiz.setTitle(request.title().trim());
        quiz.setTimeLimit(request.timeLimit());
        Quiz saved = quizRepository.save(quiz);
        log.info("Создан тест id={} для модуля id={}", saved.getId(), moduleId);
        return QuizMapper.toResponse(saved);
    }

    @Transactional
    public QuizResponse update(Long id, UpdateQuizRequest request) {
        Quiz quiz = requireQuiz(id);
        quiz.setTitle(request.title().trim());
        quiz.setTimeLimit(request.timeLimit());
        log.info("Обновлён тест id={}", id);
        return QuizMapper.toResponse(quiz);
    }

    public QuizResponse get(Long id) {
        return QuizMapper.toResponse(requireQuiz(id));
    }

    @Transactional
    public void delete(Long id) {
        Quiz quiz = requireQuiz(id);
        quizRepository.delete(quiz);
        log.info("Удалён тест id={}", id);
    }

    @Transactional
    public QuizResponse addQuestion(Long quizId, CreateQuestionRequest request) {
        Quiz quiz = requireQuiz(quizId);
        validateOptions(request);
        Question question = new Question();
        question.setQuiz(quiz);
        question.setText(request.text().trim());
        question.setType(request.type());
        for (CreateAnswerOptionRequest optionRequest : request.options()) {
            AnswerOption option = new AnswerOption();
            option.setText(optionRequest.text().trim());
            option.setCorrect(optionRequest.correct());
            question.addOption(option);
        }
        quiz.addQuestion(question);
        questionRepository.save(question);
        log.info("В тест id={} добавлен вопрос id={}", quizId, question.getId());
        return QuizMapper.toResponse(quiz);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = Lookups.required(
                questionRepository.findById(questionId),
                "Вопрос с id=" + questionId + " не найден");
        questionRepository.delete(question);
        log.info("Удалён вопрос id={}", questionId);
    }

    @Transactional
    public QuizAttemptResponse take(Long quizId, TakeQuizRequest request) {
        Quiz quiz = requireQuiz(quizId);
        User student = userService.requireUser(request.studentId());
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleException("Пройти тест может только студент");
        }
        Course course = quiz.getModule().getCourse();
        enrollmentPolicy.requireEnrolled(student.getId(), course.getId());
        List<Question> questions = quiz.getQuestions();
        if (questions.isEmpty()) {
            throw new BusinessRuleException("В тесте нет вопросов");
        }
        Map<Long, Set<Long>> selected = QuizAnswerValidator.validate(questions, request.answers());
        QuizGrader.QuizScore score = QuizGrader.grade(questions, selected);
        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setScore(score.score());
        submission.setTakenAt(LocalDateTime.now(clock));
        QuizSubmission saved = quizSubmissionRepository.save(submission);
        log.info("Студент id={} прошёл тест id={} с результатом {}%", student.getId(), quizId, score.score());
        return new QuizAttemptResponse(
                saved.getId(),
                quiz.getId(),
                student.getId(),
                student.getName(),
                score.score(),
                score.correctAnswers(),
                score.totalQuestions(),
                saved.getTakenAt());
    }

    public List<QuizResultResponse> listResults(Long quizId) {
        requireQuiz(quizId);
        return quizSubmissionRepository.findByQuizId(quizId).stream().map(QuizMapper::toResult).toList();
    }

    public List<QuizResultResponse> listResultsByStudent(Long studentId) {
        userService.requireUser(studentId);
        return quizSubmissionRepository.findByStudentId(studentId).stream().map(QuizMapper::toResult).toList();
    }

    private Quiz requireQuiz(Long id) {
        return Lookups.required(quizRepository.findById(id), "Тест с id=" + id + " не найден");
    }

    private void validateOptions(CreateQuestionRequest request) {
        long correctCount = request.options().stream().filter(CreateAnswerOptionRequest::correct).count();
        if (correctCount == 0) {
            throw new BusinessRuleException("У вопроса должен быть хотя бы один правильный вариант");
        }
        if (request.type() == QuestionType.SINGLE_CHOICE && correctCount != 1) {
            throw new BusinessRuleException("У вопроса с одним ответом должен быть ровно один правильный вариант");
        }
    }
}
