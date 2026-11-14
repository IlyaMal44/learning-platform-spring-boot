package com.example.learningplatform.service;

import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Tag;
import com.example.learningplatform.dto.request.CreateTagRequest;
import com.example.learningplatform.dto.response.TagResponse;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.CatalogMapper;
import com.example.learningplatform.repository.CourseRepository;
import com.example.learningplatform.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;
    private final CourseRepository courseRepository;

    public TagService(TagRepository tagRepository, CourseRepository courseRepository) {
        this.tagRepository = tagRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public TagResponse create(CreateTagRequest request) {
        String name = request.name().trim();
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Тег «" + name + "» уже существует");
        }
        Tag tag = new Tag();
        tag.setName(name);
        Tag saved = tagRepository.save(tag);
        log.info("Создан тег id={} name={}", saved.getId(), saved.getName());
        return CatalogMapper.toTag(saved);
    }

    public List<TagResponse> list() {
        return tagRepository.findAll().stream().map(CatalogMapper::toTag).toList();
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = requireTag(id);
        List<Course> courses = courseRepository.findByTagsId(id);
        for (Course course : courses) {
            course.removeTag(tag);
        }
        courseRepository.flush();
        tagRepository.delete(tag);
        log.info("Удалён тег id={}", id);
    }

    public Tag requireTag(Long id) {
        return Lookups.required(tagRepository.findById(id), "Тег с id=" + id + " не найден");
    }
}
