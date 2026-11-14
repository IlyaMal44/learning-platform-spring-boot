package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.CreateTagRequest;
import com.example.learningplatform.dto.response.TagResponse;
import com.example.learningplatform.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Теги")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @Operation(summary = "Создать тег")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        TagResponse body = tagService.create(request);
        return ResponseEntity.created(URI.create("/api/tags/" + body.id())).body(body);
    }

    @GetMapping
    @Operation(summary = "Список тегов")
    public List<TagResponse> list() {
        return tagService.list();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тег")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
