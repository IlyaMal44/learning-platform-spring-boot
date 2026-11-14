package com.example.learningplatform.mapper;

import com.example.learningplatform.domain.Category;
import com.example.learningplatform.domain.Tag;
import com.example.learningplatform.dto.response.CategoryResponse;
import com.example.learningplatform.dto.response.TagResponse;

public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static CategoryResponse toCategory(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }

    public static TagResponse toTag(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
