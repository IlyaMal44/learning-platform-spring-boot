package com.example.learningplatform.service;

import com.example.learningplatform.domain.Lesson;
import com.example.learningplatform.domain.Module;
import com.example.learningplatform.dto.request.CreateLessonRequest;
import com.example.learningplatform.dto.request.UpdateLessonRequest;
import com.example.learningplatform.dto.response.LessonResponse;
import com.example.learningplatform.mapper.CourseMapper;
import com.example.learningplatform.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private static final Logger log = LoggerFactory.getLogger(LessonService.class);

    private final LessonRepository lessonRepository;
    private final ModuleService moduleService;

    public LessonService(LessonRepository lessonRepository, ModuleService moduleService) {
        this.lessonRepository = lessonRepository;
        this.moduleService = moduleService;
    }

    @Transactional
    public LessonResponse create(Long moduleId, CreateLessonRequest request) {
        Module module = moduleService.requireModule(moduleId);
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        apply(lesson, request.title(), request.content(), request.videoUrl());
        Lesson saved = lessonRepository.save(lesson);
        log.info("Создан урок id={} в модуле id={}", saved.getId(), moduleId);
        return CourseMapper.toLesson(saved);
    }

    @Transactional
    public LessonResponse update(Long id, UpdateLessonRequest request) {
        Lesson lesson = requireLesson(id);
        apply(lesson, request.title(), request.content(), request.videoUrl());
        log.info("Обновлён урок id={}", id);
        return CourseMapper.toLesson(lesson);
    }

    public LessonResponse get(Long id) {
        return CourseMapper.toLesson(requireLesson(id));
    }

    @Transactional
    public void delete(Long id) {
        Lesson lesson = requireLesson(id);
        lessonRepository.delete(lesson);
        log.info("Удалён урок id={}", id);
    }

    public Lesson requireLesson(Long id) {
        return Lookups.required(lessonRepository.findById(id), "Урок с id=" + id + " не найден");
    }

    private void apply(Lesson lesson, String title, String content, String videoUrl) {
        lesson.setTitle(title.trim());
        lesson.setContent(Texts.trimToNull(content));
        lesson.setVideoUrl(Texts.trimToNull(videoUrl));
    }
}
