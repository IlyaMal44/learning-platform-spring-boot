package com.example.learningplatform.service;

import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Module;
import com.example.learningplatform.dto.request.CreateModuleRequest;
import com.example.learningplatform.dto.request.UpdateModuleRequest;
import com.example.learningplatform.dto.response.ModuleResponse;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.CourseMapper;
import com.example.learningplatform.repository.ModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ModuleService {

    private static final Logger log = LoggerFactory.getLogger(ModuleService.class);

    private final ModuleRepository moduleRepository;
    private final CourseService courseService;

    public ModuleService(ModuleRepository moduleRepository, CourseService courseService) {
        this.moduleRepository = moduleRepository;
        this.courseService = courseService;
    }

    @Transactional
    public ModuleResponse create(Long courseId, CreateModuleRequest request) {
        Course course = courseService.requireCourse(courseId);
        Module module = new Module();
        module.setCourse(course);
        module.setTitle(request.title().trim());
        module.setDescription(Texts.trimToNull(request.description()));
        module.setOrderIndex(resolveOrder(courseId, request.orderIndex(), null));
        Module saved = moduleRepository.save(module);
        log.info("Создан модуль id={} в курсе id={}", saved.getId(), courseId);
        return CourseMapper.toModule(saved);
    }

    @Transactional
    public ModuleResponse update(Long id, UpdateModuleRequest request) {
        Module module = requireModule(id);
        module.setTitle(request.title().trim());
        module.setDescription(Texts.trimToNull(request.description()));
        if (request.orderIndex() != null) {
            module.setOrderIndex(resolveOrder(module.getCourse().getId(), request.orderIndex(), id));
        }
        log.info("Обновлён модуль id={}", id);
        return CourseMapper.toModule(module);
    }

    public ModuleResponse get(Long id) {
        return CourseMapper.toModule(requireModule(id));
    }

    @Transactional
    public void delete(Long id) {
        Module module = requireModule(id);
        moduleRepository.delete(module);
        log.info("Удалён модуль id={}", id);
    }

    public Module requireModule(Long id) {
        return Lookups.required(moduleRepository.findById(id), "Модуль с id=" + id + " не найден");
    }

    private int resolveOrder(Long courseId, Integer requested, Long currentModuleId) {
        List<Module> existing = moduleRepository.findByCourseId(courseId);
        if (requested == null) {
            return existing.stream().mapToInt(Module::getOrderIndex).max().orElse(-1) + 1;
        }
        boolean taken = existing.stream()
                .anyMatch(module -> module.getOrderIndex() == requested && !module.getId().equals(currentModuleId));
        if (taken) {
            throw new ConflictException("В курсе уже есть модуль с порядковым номером " + requested);
        }
        return requested;
    }
}
