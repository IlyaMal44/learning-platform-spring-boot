package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.UpdateReviewRequest;
import com.example.learningplatform.dto.response.ReviewResponse;
import com.example.learningplatform.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Отзывы")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить отзыв")
    public ReviewResponse get(@PathVariable Long id) {
        return reviewService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить отзыв")
    public ReviewResponse update(@PathVariable Long id, @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отзыв")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
